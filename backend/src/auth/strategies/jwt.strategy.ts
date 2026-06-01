import { Injectable, UnauthorizedException } from '@nestjs/common';
import { PassportStrategy } from '@nestjs/passport';
import { ExtractJwt, Strategy } from 'passport-jwt';
import { UsuariosService } from '../../usuarios/usuarios.services';


export const JWT_STRATEGY_NAME = 'jwt';
@Injectable()
export class JwtStrategy extends PassportStrategy(Strategy) {
  constructor(private readonly usuariosService: UsuariosService) {
    super({
      // 1. Extrae el token JWT desde la cabecera 'Authorization' como un Bearer Token
      jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
      // 2. Si el token ha expirado, rechaza la petición automáticamente (401 Unauthorized)
      ignoreExpiration: false,
      // 3. La misma clave secreta que configuramos en el AuthModule para firmar los tokens
      secretOrKey: 'SUPER_SECRET_KEY_ECOWASH_2026',
    });
  }

  /**
   * Este método se ejecuta AUTOMÁTICAMENTE una vez que Passport comprueba que el token es válido.
   * @param payload El JSON interno que venía encriptado dentro del JWT (id y correo).
   */
  async validate(payload: { sub: string; email: string }) {
    // Buscamos al usuario en la base de datos usando el ID (sub) para verificar que aún exista
    const usuario = await this.usuariosService.obtenerPorId(payload.sub);
    
    if (!usuario) {
      throw new UnauthorizedException('El token no pertenece a un usuario válido');
    }

    // Lo que retornes aquí se inyectará automáticamente en el objeto Request de la petición (req.user)
    return {
      id: usuario.id,
      correo: usuario.correo,
      nombreCompleto: usuario.nombre_completo,
      rol: usuario.rol,
    };
  }

  


}