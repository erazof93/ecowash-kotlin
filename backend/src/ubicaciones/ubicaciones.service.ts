import { Injectable, BadRequestException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class UbicacionesService {
  constructor(private readonly prisma: PrismaService) {}

  async guardarUbicacionNativa(lavadorId: string, latitud: number, longitud: number) {
    try {
      // 🚀 SQL Nativo de PostGIS con ON CONFLICT (UPSERT)
      // Si el lavador ya está en la tabla, actualiza su coordenada y tiempo; si no, lo inserta por primera vez.
      await this.prisma.$queryRaw`
        INSERT INTO ubicaciones_lavadores (lavador_id, coordenada, ultima_actualizacion)
        VALUES (
          ${lavadorId}::uuid,
          ST_SetSRID(ST_MakePoint(${longitud}, ${latitud}), 4326),
          CURRENT_TIMESTAMP
        )
        ON CONFLICT (lavador_id) 
        DO UPDATE SET 
          coordenada = ST_SetSRID(ST_MakePoint(${longitud}, ${latitud}), 4326),
          ultima_actualizacion = CURRENT_TIMESTAMP;
      `;

      return { success: true };
    } catch (error) {
      console.error('Error al guardar ubicación en PostGIS vía WebSockets:', error);
      throw new BadRequestException('Error de base de datos geoespacial');
    }
  }
}