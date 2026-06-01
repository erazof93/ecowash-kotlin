import { Module } from '@nestjs/common';
import { UbicacionesService } from './ubicaciones.service';
import { PrismaService } from 'src/prisma/prisma.service';
import { UbicacionesGateway } from './ubicaciones.gateway';

@Module({
  providers: [UbicacionesGateway, UbicacionesService, PrismaService]
})
export class UbicacionesModule {}
