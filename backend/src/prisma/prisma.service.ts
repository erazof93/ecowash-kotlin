import { Injectable, OnModuleInit, OnModuleDestroy } from '@nestjs/common';
import { PrismaClient } from '../../generated/prisma/client'; // Asegúrate de que esta ruta sea correcta según tu estructura de carpetas

@Injectable()
export class PrismaService extends PrismaClient implements OnModuleInit, OnModuleDestroy {
  
  async onModuleInit() {
    await this.$connect();
    console.log('✅ Conexión limpia establecida con db_ecowash en Docker');
  }

  async onModuleDestroy() {
    await this.$disconnect();
    console.log('🛑 Conexión con PostgreSQL cerrada de manera segura');
  }
}