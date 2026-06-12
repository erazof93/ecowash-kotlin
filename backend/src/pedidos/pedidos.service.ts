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

  async aceptarPedido(pedidoId: string, lavadorId: string) {
    const pedido: any = await this.prisma.$queryRaw`
      SELECT id, cliente_id, lavador_id, estado FROM pedidos WHERE id = ${pedidoId}::uuid
    `;

    if (!pedido || pedido.length === 0) {
      throw new NotFoundException('Pedido no encontrado');
    }

    const p = pedido[0];
    if (p.estado !== 'PENDIENTE') {
      throw new BadRequestException('Este pedido ya no esta disponible');
    }
    if (p.lavador_id && p.lavador_id.toString() !== lavadorId) {
      throw new BadRequestException('Este pedido ya fue asignado a otro lavador');
    }

    await this.prisma.$executeRawUnsafe(
      `UPDATE pedidos SET estado = 'ACEPTADO'::estado_pedido_enum, lavador_id = $1::uuid, actualizado_at = NOW() WHERE id = $2::uuid`,
      lavadorId,
      pedidoId
    );

    return { success: true, message: 'Pedido aceptado correctamente' };
  }

  async cambiarEstado(pedidoId: string, lavadorId: string, estadoActual: string, estadoNuevo: string) {
    const pedido: any = await this.prisma.$queryRaw`
      SELECT id, lavador_id, estado FROM pedidos WHERE id = ${pedidoId}::uuid
    `;

    if (!pedido || pedido.length === 0) {
      throw new NotFoundException('Pedido no encontrado');
    }

    const p = pedido[0];
    if (p.lavador_id?.toString() !== lavadorId) {
      throw new BadRequestException('No eres el lavador asignado a este pedido');
    }
    if (p.estado !== estadoActual) {
      throw new BadRequestException(`El pedido debe estar en estado ${estadoActual}`);
    }

    if (estadoNuevo === 'LAVANDO') {
      await this.prisma.$executeRawUnsafe(
        `UPDATE pedidos SET estado = 'LAVANDO'::estado_pedido_enum, lavado_iniciado_at = NOW(), actualizado_at = NOW() WHERE id = $1::uuid`,
        pedidoId
      );
    } else {
      await this.prisma.$executeRawUnsafe(
        `UPDATE pedidos SET estado = $1::estado_pedido_enum, actualizado_at = NOW() WHERE id = $2::uuid`,
        estadoNuevo,
        pedidoId
      );
    }

    return { success: true, message: `Pedido actualizado a ${estadoNuevo}` };
  }

  async crearPedido(clienteId: string, dto: CreatePedidoDto) {
    const usuario = await this.prisma.usuarios.findUnique({
      where: { id: clienteId },
    });

    if (!usuario || usuario.rol !== 'CLIENTE') {
      throw new BadRequestException(
        'El usuario no está registrado como un cliente válido',
      );
    }

    let clienteExiste = await this.prisma.clientes.findUnique({
      where: { usuario_id: clienteId },
    });

    if (!clienteExiste) {
      clienteExiste = await this.prisma.clientes.create({
        data: { usuario_id: clienteId },
      });
    }

    const tarifa = await this.prisma.precios_servicios.findUnique({
      where: { id: dto.precio_servicio_id },
    });

    if (!tarifa) {
      throw new NotFoundException(
        'La tarifa seleccionada no existe en el sistema',
      );
    }

    const configComision = await this.prisma.configuracion_global.findUnique({
      where: { clave: 'porcentaje_comision' },
    });
    const porcentajeComision = configComision
      ? Number(configComision.valor)
      : 0.15;

    const precioTotal = Number(tarifa.precio);
    const comisionCalculada = precioTotal * porcentajeComision;

    try {
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

  async obtenerTodos() {
    return this.prisma.pedidos.findMany({
      orderBy: { creado_at: 'desc' },
    });
  }

  async obtenerPendientes() {
    return this.prisma.pedidos.findMany({
      where: { estado: 'PENDIENTE' },
      orderBy: { creado_at: 'desc' },
    });
  }

  async obtenerPedidosCercanos(dto: GetCercanosDto) {
    const lat = parseFloat(dto.latitud);
    const lng = parseFloat(dto.longitud);
    const radioMetros = parseFloat(dto.radioKm) * 1000;

    try {
      const pedidosCercanos = await this.prisma.$queryRaw`
        SELECT 
          id::text,
          cliente_id::text,
          precio_servicio_id::text,
          estado::text,
          direccion_texto,
          precio_total::float8,
          comision_calculada::float8,
          TO_CHAR(creado_at, 'YYYY-MM-DD"T"HH24:MI:SS') AS creado_at,
          TO_CHAR(actualizado_at, 'YYYY-MM-DD"T"HH24:MI:SS') AS actualizado_at,
          ST_Y(ubicacion_cliente)::float8 AS latitud,
          ST_X(ubicacion_cliente)::float8 AS longitud,
          ST_Distance(
            ubicacion_cliente,
            ST_SetSRID(ST_MakePoint(${lng}, ${lat}), 4326)::geography
          ) AS distancia_metros
        FROM pedidos
        WHERE 
          estado = 'PENDIENTE'::estado_pedido_enum
          AND ST_DWithin(
            ubicacion_cliente,
            ST_SetSRID(ST_MakePoint(${lng}, ${lat}), 4326)::geography,
            ${radioMetros}
          )
        ORDER BY distancia_metros ASC;
      `;

      return pedidosCercanos;
    } catch (error) {
      console.error('Error en la consulta espacial de cercanía:', error);
      throw new BadRequestException('Error al procesar la búsqueda por coordenadas');
    }
  }
}
