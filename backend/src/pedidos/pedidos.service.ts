import {
  Injectable,
  NotFoundException,
  BadRequestException,
} from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreatePedidoDto } from './dto/create-pedido.dto';
import { GetCercanosDto } from './dto/get-cercanos.dto';


@Injectable()
export class PedidosService {
  constructor(private readonly prisma: PrismaService) {}

  async crearPedido(clienteId: string, dto: CreatePedidoDto) {
    // 1. Validar que el cliente exista en la tabla satélite 'clientes'
    const clienteExiste = await this.prisma.clientes.findUnique({
      where: { usuario_id: clienteId },
    });

    if (!clienteExiste) {
      throw new BadRequestException(
        'El usuario no está registrado como un cliente válido',
      );
    }

    // 2. Buscar la tarifa oficial en la Matriz de Precios
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

    // 3. Convertir el precio a número para calcular las finanzas congeladas
    const precioTotal = Number(tarifa.precio);
    const comisionCalculada = precioTotal * porcentajeComision;

    try {
      // 4. Inserción Espacial con SQL Nativo vía Prisma $queryRaw
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

      // Como $queryRaw devuelve un array, retornamos el primer registro creado
      return nuevoPedido[0];
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
