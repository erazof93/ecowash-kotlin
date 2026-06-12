import {
  Injectable,
  NotFoundException,
  BadRequestException,
} from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreatePedidoDto } from './dto/create-pedido.dto';
import { GetCercanosDto } from './dto/get-cercanos.dto';
import { EventEmitter2 } from '@nestjs/event-emitter';


@Injectable()
export class PedidosService {
  constructor(private readonly prisma: PrismaService, private readonly eventEmitter: EventEmitter2) {}

  async cancelarPedido(pedidoId: string, userId: string) {
    const pedido: any = await this.prisma.$queryRaw`
      SELECT id, cliente_id, lavador_id, estado FROM pedidos WHERE id = ${pedidoId}::uuid
    `;

    if (!pedido || pedido.length === 0) {
      throw new NotFoundException('Pedido no encontrado');
    }

    const p = pedido[0];

    if (p.cliente_id?.toString() !== userId && p.lavador_id?.toString() !== userId) {
      throw new BadRequestException('No tienes permiso para cancelar este pedido');
    }

    if (p.estado !== 'PENDIENTE') {
      throw new BadRequestException('Solo se pueden cancelar pedidos en estado PENDIENTE');
    }

    await this.prisma.$executeRaw`
      UPDATE pedidos SET estado = 'CANCELADO'::estado_pedido_enum, actualizado_at = NOW()
      WHERE id = ${pedidoId}::uuid
    `;

    return { success: true, message: 'Pedido cancelado correctamente' };
  }

  async crearPedido(clienteId: string, dto: CreatePedidoDto) {
    // 1. Validar que el usuario exista y tenga rol CLIENTE
    const usuario = await this.prisma.usuarios.findUnique({
      where: { id: clienteId },
    });

    if (!usuario || usuario.rol !== 'CLIENTE') {
      throw new BadRequestException(
        'El usuario no está registrado como un cliente válido',
      );
    }

    // 2. Verificar si tiene fila en la tabla satélite 'clientes'; si no, crearla
    let clienteExiste = await this.prisma.clientes.findUnique({
      where: { usuario_id: clienteId },
    });

    if (!clienteExiste) {
      clienteExiste = await this.prisma.clientes.create({
        data: { usuario_id: clienteId },
      });
    }

    // 3. Buscar la tarifa oficial en la Matriz de Precios
    const tarifa = await this.prisma.precios_servicios.findUnique({
      where: { id: dto.precio_servicio_id },
    });

    if (!tarifa) {
      throw new NotFoundException(
        'La tarifa seleccionada no existe en el sistema',
      );
    }

    //tarifa desde la base de datos
    const configComision = await this.prisma.configuracion_global.findUnique({
      where: { clave: 'porcentaje_comision' },
    });
    const porcentajeComision = configComision
      ? Number(configComision.valor)
      : 0.15;

    // 4. Convertir el precio a número para calcular las finanzas congeladas
    const precioTotal = Number(tarifa.precio);
    const comisionCalculada = precioTotal * porcentajeComision;

    try {
      // 5. Inserción Espacial con SQL Nativo vía Prisma $queryRaw
      // Usamos ST_SetSRID y ST_MakePoint para transformar las coordenadas del GPS en geometría pura
      const nuevoPedido: any[] = await this.prisma.$queryRaw`
        INSERT INTO pedidos (
          cliente_id, 
          precio_servicio_id, 
          direccion_texto, 
          ubicacion_cliente, 
          precio_total, 
          comision_calculada,
          estado
        ) VALUES (
          ${clienteId}::uuid, 
          ${dto.precio_servicio_id}::uuid, 
          ${dto.direccion_texto}, 
          ST_SetSRID(ST_MakePoint(${dto.longitud}, ${dto.latitud}), 4326), 
          ${precioTotal}, 
          ${comisionCalculada},
          'PENDIENTE'::estado_pedido_enum
        )
        RETURNING id, cliente_id, precio_servicio_id, estado, direccion_texto, precio_total, comision_calculada, creado_at;
      `;

      const pedidoGuardado = nuevoPedido[0];

      // 6. Disparar el evento al ecosistema con los datos clave (ID y coordenadas)
      this.eventEmitter.emit('pedido.creado', {
        pedido: pedidoGuardado,
        latitud: dto.latitud,
        longitud: dto.longitud
      });

      return pedidoGuardado;

    } catch (error) {
      console.error('Error al insertar el pedido espacial:', error);
      throw new BadRequestException(
        'No se pudo procesar la ubicación geográfica del pedido',
      );
    }

    


  }

  // Listar todos los pedidos (Útil para el panel de administración o historial global)
  async obtenerTodos() {
    return this.prisma.pedidos.findMany({
      orderBy: { creado_at: 'desc' },
    });
  }


  //tratamiento con postgis

  async obtenerPedidosCercanos(dto: GetCercanosDto) {
  const lat = parseFloat(dto.latitud);
  const lng = parseFloat(dto.longitud);
  const radioMetros = parseFloat(dto.radioKm) * 1000; // 🗺️ PostGIS opera en metros

  try {
    // Query SQL Nativo para explotar los índices espaciales de PostGIS
    const pedidosCercanos = await this.prisma.$queryRaw`
      SELECT 
        id,
        cliente_id,
        precio_servicio_id,
        estado,
        direccion_texto,
        precio_total,
        creado_at,
        -- 📏 Calculamos la distancia exacta en metros entre el cliente y el lavador
        ST_Distance(
          ubicacion_cliente,
          ST_SetSRID(ST_MakePoint(${lng}, ${lat}), 4326)::geography
        ) AS distancia_metros
      FROM pedidos
      WHERE 
        estado = 'PENDIENTE'::estado_pedido_enum
        -- 🎯 Filtramos solo los que estén dentro del radio (ST_DWithin usa metros sobre geography)
        AND ST_DWithin(
          ubicacion_cliente,
          ST_SetSRID(ST_MakePoint(${lng}, ${lat}), 4326)::geography,
          ${radioMetros}
        )
      ORDER BY distancia_metros ASC; -- 🧭 Del más cercano al más lejano
    `;

    return pedidosCercanos;
  } catch (error) {
    console.error('Error en la consulta espacial de cercanía:', error);
    throw new BadRequestException('Error al procesar la búsqueda por coordenadas');
  }
}
}
