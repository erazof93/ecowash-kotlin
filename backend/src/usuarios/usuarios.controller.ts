import { Controller, Get, Post, Body, Patch, Param, Delete, ParseUUIDPipe, UseGuards } from '@nestjs/common';
import { UsuariosService } from './usuarios.services';
import { RegisterDto } from '../auth/dto/register.dto';
import { UpdateUsuarioDto } from './dto/update-usuario';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { RolesGuard, Roles } from '../auth/guards/roles.guard';
import { rol_enum } from '@prisma/client';

@Controller('usuarios') // Prefijo global: http://localhost:3000/usuarios
@UseGuards(JwtAuthGuard, RolesGuard) // 🔒 JWT + Roles
export class UsuariosController {
  constructor(private readonly usuariosService: UsuariosService) {}

  @Post()
  async crear(@Body() registerDto: RegisterDto) {
    return this.usuariosService.crearUsuario(registerDto);
  }

  @Get()
  @Roles(rol_enum.ADMIN) // 🔒 Solo ADMIN puede listar todos los usuarios
  async obtenerTodos() {
    return this.usuariosService.obtenerTodos();
  }

  // Usamos ParseUUIDPipe para que Nest valide que el ID sea un UUID válido antes de entrar al servicio
  @Get(':id')
  async obtenerUno(@Param('id', ParseUUIDPipe) id: string) {
    return this.usuariosService.obtenerPorId(id);
  }

  @Patch(':id')
  async actualizar(@Param('id', ParseUUIDPipe) id: string, @Body() updateUsuarioDto: UpdateUsuarioDto) {
    return this.usuariosService.actualizarUsuario(id, updateUsuarioDto);
  }

  @Delete(':id')
  async eliminar(@Param('id', ParseUUIDPipe) id: string) {
    return this.usuariosService.eliminarUsuario(id);
  }
}