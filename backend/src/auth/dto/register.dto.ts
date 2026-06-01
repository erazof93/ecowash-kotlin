import { IsEmail, IsNotEmpty, IsString, MinLength, IsOptional, IsUrl, IsEnum } from 'class-validator';
import { rol_enum } from '@prisma/client';

export class RegisterDto{
    @IsEmail({}, {message: 'El correo electrónico no es válido'})
    @IsNotEmpty({message: 'El correo electrónico es obligatorio'})
    correo!: string;

    @IsString({message: 'La contraseña debe ser una cadena de texto'})
    @MinLength(6, {message: 'La contraseña debe tener al menos 6 caracteres'})
    @IsNotEmpty({message: 'La contraseña es obligatoria'})
    contrasena!: string;

    @IsString({ message: 'El nombre debe ser una cadena de texto' })
    @IsNotEmpty({ message: 'El nombre completo es obligatorio' })
    nombre_completo!: string;

    @IsString({ message: 'El teléfono debe ser una cadena de texto' })
    @IsOptional()
    telefono?: string;

    @IsUrl({}, { message: 'La URL de la foto no es válida' })
    @IsOptional()
  fotoUrl?: string;

  @IsEnum(rol_enum, { message: 'El rol proporcionado no es válido (ADMIN, CLIENTE, LAVADOR)' })
  @IsOptional()
  rol?: rol_enum; // 👈 Campo opcional, si no viene, Postgres le pondrá 'CLIENTE'
}