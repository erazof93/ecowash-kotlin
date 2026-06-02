import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { UsuariosModule } from './usuarios/usuarios.module';
import { AuthModule } from './auth/auth.module';
import { PrismaModule } from './prisma/prisma.module';
import { PreciosModule } from './precios/precios.module';
import { PedidosModule } from './pedidos/pedidos.module';
import { UbicacionesModule } from './ubicaciones/ubicaciones.module';
import { EventEmitterModule } from '@nestjs/event-emitter';

@Module({
  imports: [EventEmitterModule.forRoot(), PrismaModule, AuthModule, UsuariosModule, PreciosModule, PedidosModule, UbicacionesModule],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
