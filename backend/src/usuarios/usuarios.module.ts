import { Module } from "@nestjs/common";
import { UsuariosController } from './usuarios.controller';
import { UsuariosService } from './usuarios.services';

@Module({
  controllers: [UsuariosController],
  providers: [UsuariosService],
  exports: [UsuariosService],
})
export class UsuariosModule {}