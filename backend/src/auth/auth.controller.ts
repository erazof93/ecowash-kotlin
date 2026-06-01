import { Controller, Post, Body, HttpCode, HttpStatus } from '@nestjs/common';
import { AuthService } from './auth.service';
import { RegisterDto } from './dto/register.dto';
import { LoginDto } from './dto/login.dto';

@Controller('auth') // La ruta base será http://localhost:3000/auth
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  // Endpoint de Registro: POST /auth/register
  @Post('register')
  async registrar(@Body() registerDto: RegisterDto) {
    return this.authService.register(registerDto);
  }

  // Endpoint de Login: POST /auth/login
  @Post('login')
  @HttpCode(HttpStatus.OK) // Por defecto los POST en Nest devuelven 201, cambiamos a 200 para el login
  async ingresar(@Body() loginDto: LoginDto) {
    return this.authService.login(loginDto);
  }
}