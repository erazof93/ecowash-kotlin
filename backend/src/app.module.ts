import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { UsuariosModule } from './usuarios/usuarios.module';
import { AuthModule } from './auth/auth.module';
import { PrismaModule } from './prisma/prisma.module';
import { PreciosModule } from './precios/precios.module';
import { PedidosModule } from './pedidos/pedidos.module';

@Module({
  imports: [PrismaModule, AuthModule, UsuariosModule, PreciosModule, PedidosModule],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
