import { Injectable, OnModuleInit, OnModuleDestroy } from '@nestjs/common';
// Importamos el PrismaClient generado por Prisma (la carpeta generate y apuntamos a client)
import { PrismaClient } from 'src/generated/prisma/client';

@Injectable()
export class PrismaService extends PrismaClient implements OnModuleInit, OnModuleDestroy {
  
  // Evento de ciclo de vida de NestJS: Se ejecuta al iniciar el módulo
  async onModuleInit() {
    // Intenta la conexión utilizando la URL del .env
    await this.$connect();
    console.log('✅ Conexión establecida con db_ecowash en Docker (ecowash_user)');
  }

  // Evento de ciclo de vida de NestJS: Se ejecuta al apagar la aplicación
  async onModuleDestroy() {
    await this.$disconnect();
    console.log('🛑 Conexión con PostgreSQL cerrada de manera segura');
  }
}