import { IsNotEmpty, IsNumberString } from 'class-validator';

export class GetCercanosDto {
  @IsNotEmpty({ message: 'La latitud del lavador es obligatoria' })
  latitud!: string;

  @IsNotEmpty({ message: 'La longitud del lavador es obligatoria' })
  longitud!: string;

  @IsNotEmpty({ message: 'El radio de búsqueda en kilómetros es obligatorio' })
  radioKm!: string;
}