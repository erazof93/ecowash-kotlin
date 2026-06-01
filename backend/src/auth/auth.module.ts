import { Module } from "@nestjs/common";
import { AuthService } from './auth.service';
import { AuthController } from './auth.controller';
import { UsuariosModule } from '../usuarios/usuarios.module';
import { JwtModule } from '@nestjs/jwt';
import { PassportModule } from '@nestjs/passport';
import { JwtStrategy } from './strategies/jwt.strategy';

@Module({
  imports: [
    UsuariosModule, // Requerido para usar el UsuariosService
    PassportModule,
    JwtModule.register({
      secret: 'SUPER_SECRET_KEY_ECOWASH_2026', // Clave para firmar los JWT
      signOptions: { expiresIn: '7d' }, // El token durará 7 días en el celular del usuario
    }),
  ],
  controllers: [AuthController],
  providers: [AuthService, JwtStrategy],
})
export class AuthModule {}