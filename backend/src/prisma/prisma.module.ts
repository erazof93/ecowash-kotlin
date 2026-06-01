import { Global, Module } from '@nestjs/common';
import { PrismaService } from './prisma.service';

@Global()
@Module({
  providers: [PrismaService],
  exports: [PrismaService], // Permite que otros servicios (Auth, Pedidos) usen la conexión
})
export class PrismaModule {}