import {
  Injectable,
  NotFoundException,
  BadRequestException,
} from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { RegisterDto } from '../auth/dto/register.dto';
import { UpdateUsuarioDto } from './dto/update-usuario';
import * as bcrypt from 'bcrypt';

@Injectable()
export class UsuariosService {
  constructor(private readonly prisma: PrismaService) {}

  // 1. CREATE (Este lo usará el Auth posterior, pero lo dejamos listo con encriptación interna directa por ahora)
  async crearUsuario(data: RegisterDto) {
    const usuarioExiste = await this.buscarPorCorreo(data.correo);
    if (usuarioExiste) {
      throw new BadRequestException('El correo electrónico ya está registrado');
    }

    const saltRounds = 10;
    const contrasenaEncriptada = await bcrypt.hash(data.contrasena, saltRounds);

    return this.prisma.usuarios.create({
      data: {
        correo: data.correo,
        contrasena_hash: contrasenaEncriptada,
        nombre_completo: data.nombre_completo,
        telefono: data.telefono || null,
        foto_url: data.fotoUrl || null,
        google_id: null,
        rol: data.rol
      },
    });
  }

  // 2. READ ALL
  async obtenerTodos() {
    return this.prisma.usuarios.findMany({
      select: {
        id: true,
        correo: true,
        nombre_completo: true, // Lo ideal es mapearlo a camelCase en el futuro, pero por ahora lo aseguramos
        telefono: true,
        foto_url: true,
        creado_at: true,
        rol: true,
        // ❌ Al NO poner contrasena_hash aquí, Prisma la excluye automáticamente
      },
      orderBy: { creado_at: 'desc' },
    });
  }

  // 3. READ ONE (Por ID - UUID)
  async obtenerPorId(id: string) {
    const usuario = await this.prisma.usuarios.findUnique({
      where: { id },
      select: {
      id: true,
      correo: true,
      nombre_completo: true,
      telefono: true,
      foto_url: true,
      creado_at: true,
      rol: true,
    },
    });
    if (!usuario) {
      throw new NotFoundException(`Usuario con ID ${id} no encontrado`);
    }
    return usuario;
  }

  // Helper interno para buscar por correo
  async buscarPorCorreo(correo: string) {
    return this.prisma.usuarios.findUnique({
      where: { correo },
    });
  }

  // 4. UPDATE
  async actualizarUsuario(id: string, updateUsuarioDto: UpdateUsuarioDto) {
    // Verificar si el usuario existe antes de actualizar
    await this.obtenerPorId(id);

    // Objeto temporal para armar los datos que se van a guardar
    const datosActualizar: any = {};

    if (updateUsuarioDto.correo)
      datosActualizar.correo = updateUsuarioDto.correo;
    if (updateUsuarioDto.nombre_completo)
      datosActualizar.nombre_completo = updateUsuarioDto.nombre_completo;
    if (updateUsuarioDto.telefono)
      datosActualizar.telefono = updateUsuarioDto.telefono;
    if (updateUsuarioDto.fotoUrl)
      datosActualizar.foto_url = updateUsuarioDto.fotoUrl;

    // Si viene una nueva contraseña, le aplicamos el hash seguro antes de guardarla
    if (updateUsuarioDto.contrasena) {
      const saltRounds = 10;
      datosActualizar.contrasena_hash = await bcrypt.hash(
        updateUsuarioDto.contrasena,
        saltRounds,
      );
    }

    // Guardar los cambios reflejando el formato de la Base de Datos
    return this.prisma.usuarios.update({
      where: { id },
      data: datosActualizar,
    });
  }

  // 5. DELETE
  async eliminarUsuario(id: string) {
    // Verificar si existe antes de borrar
    await this.obtenerPorId(id);

    await this.prisma.usuarios.delete({
      where: { id },
    });

    return { message: `Usuario con ID ${id} eliminado correctamente` };
  }
}
