import { Module } from '@nestjs/common';
import { PreciosService } from './precios.service';
import { PreciosController } from './precios.controller';
import { PrismaService } from 'src/prisma/prisma.service';

@Module({
  controllers: [PreciosController],
  providers: [PreciosService, PrismaService],
})
export class PreciosModule {}
