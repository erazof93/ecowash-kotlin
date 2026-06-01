import { IsEmail, IsEnum, IsOptional, IsString, IsUrl, MinLength } from 'class-validator';
import { rol_enum } from '@prisma/client'; // 👈 Importamos el enum

export class UpdateUsuarioDto {
  @IsEmail({}, { message: 'El formato del correo electrónico no es válido' })
  @IsOptional()
  correo?: string;

  @IsString({ message: 'La contraseña debe ser una cadena de texto' })
  @MinLength(6, { message: 'La nueva contraseña debe tener al menos 6 caracteres' })
  @IsOptional()
  contrasena?: string; // Si quiere cambiar su contraseña

  @IsString({ message: 'El nombre debe ser una cadena de texto' })
  @IsOptional()
  nombre_completo?: string;

  @IsString({ message: 'El teléfono debe ser una cadena de texto' })
  @IsOptional()
  telefono?: string;

  @IsUrl({}, { message: 'La URL de la foto no es válida' })
  @IsOptional()
  fotoUrl?: string;

  @IsEnum(rol_enum, { message: 'El rol proporcionado no es válido' })
  @IsOptional()
  rol?: rol_enum; // 👈 Permite actualizar el rol
}