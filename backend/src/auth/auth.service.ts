import {
  Injectable,
  BadRequestException,
  UnauthorizedException,
} from '@nestjs/common';
import { UsuariosService } from '../usuarios/usuarios.services';
import { JwtService } from '@nestjs/jwt';
import { RegisterDto } from './dto/register.dto';
import { LoginDto } from './dto/login.dto';
import * as bcrypt from 'bcrypt';

@Injectable()
export class AuthService {
  constructor(
    private readonly usuariosService: UsuariosService, // Inyectamos el servicio de usuarios
    private readonly jwtService: JwtService, // Inyectamos el generador de JWT
  ) {}

  // 1. REGISTRO DE USUARIOS
  async register(registerDto: RegisterDto) {
    // Verificar si el correo ya existe en la DB
    const usuarioExiste = await this.usuariosService.buscarPorCorreo(
      registerDto.correo,
    );
    if (usuarioExiste) {
      throw new BadRequestException('El correo electrónico ya está registrado');
    }

    // Guardar en la base de datos a través del servicio de usuarios
    // (crearUsuario se encarga de hashear la contraseña internamente)
    const nuevoUsuario = await this.usuariosService.crearUsuario(registerDto);

    // Retornamos el usuario creado y su Token de acceso de inmediato
    return {
      usuario: {
        id: nuevoUsuario.id,
        correo: nuevoUsuario.correo,
        nombreCompleto: nuevoUsuario.nombre_completo,
        rol: nuevoUsuario.rol,
      },
      token: this.generarJwt(nuevoUsuario.id, nuevoUsuario.correo),
    };
  }

  // 2. INICIO DE SESIÓN (LOGIN)
  async login(loginDto: LoginDto) {
    // 1. Buscar si el usuario existe por su correo
    const usuario = await this.usuariosService.buscarPorCorreo(loginDto.correo);
    if (!usuario) {
      throw new UnauthorizedException(
        'Credenciales incorrectas (Correo no encontrado)',
      );
    }

    // 🛡️ CONTROL DE SEGURIDAD: Si no tiene contraseña guardada, significa que es una cuenta de Google
    if (!usuario.contrasena_hash) {
      throw new BadRequestException(
        'Esta cuenta fue creada con el botón de Google. Por favor, inicia sesión presionando "Continuar con Google".',
      );
    }

    // 2. Comparar la contraseña ingresada con el Hash de la DB
    // ¡TypeScript ya no se queja porque la validación de arriba garantiza que es un string real!
    const contrasenaValida = await bcrypt.compare(
      loginDto.contrasena,
      usuario.contrasena_hash,
    );
    if (!contrasenaValida) {
      throw new UnauthorizedException(
        'Credenciales incorrectas (Contraseña inválida)',
      );
    }

    // 3. Retornar perfil y su Token JWT
    return {
      usuario: {
        id: usuario.id,
        correo: usuario.correo,
        nombreCompleto: usuario.nombre_completo,
        rol: usuario.rol,
      },
      token: this.generarJwt(usuario.id, usuario.correo),
    };
  }

  // Función interna para firmar el token JWT
  private generarJwt(id: string, correo: string) {
    const payload = { sub: id, email: correo };
    return this.jwtService.sign(payload);
  }
}
