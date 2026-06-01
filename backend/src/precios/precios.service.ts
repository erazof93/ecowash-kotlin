import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class PreciosService {
  constructor(private readonly prisma: PrismaService) {}

  // 1. Obtener la matriz completa de precios con sus relaciones
  async obtenerMatrizPrecios() {
    return this.prisma.precios_servicios.findMany({
      include: {
        categorias_vehiculos: {
          select: {
            nombre: true,
            descripcion: true,
          },
        },
        servicios: {
          select: {
            nombre: true,
            descripcion: true,
          },
        },
      },
    });
  }

  // 2. Obtener solo las categorías de vehículos (Útil para llenar el primer Dropdown en Flutter)
  async obtenerCategorias() {
    return this.prisma.categorias_vehiculos.findMany({
      orderBy: { nombre: 'asc' },
    });
  }

  // 3. Buscar un precio específico por combinación (Para cuando el backend procese un pedido real)
  async buscarPrecioEspecifico(categoriaId: string, servicioId: string) {
    const precioTarifa = await this.prisma.precios_servicios.findFirst({
      where: {
        categoria_id: categoriaId,
        servicio_id: servicioId,
      },
    });

    if (!precioTarifa) {
      throw new NotFoundException('No se encontró una tarifa para la combinación seleccionada');
    }

    return precioTarifa;
  }
}
