import { IsNotEmpty, IsUUID, IsString, IsNumber, Min, Max } from 'class-validator';

export class CreatePedidoDto {
  @IsUUID('4', { message: 'El id de la tarifa debe ser un UUID válido' })
  @IsNotEmpty({ message: 'La tarifa del servicio (precio_servicio_id) es obligatoria' })
  precio_servicio_id!: string; // 👈 Idéntico a tu columna SQL

  @IsString()
  @IsNotEmpty({ message: 'La dirección en texto es obligatoria para el lavador' })
  direccion_texto!: string; // 👈 Idéntico a tu columna SQL

  // Mantenemos latitud y longitud por separado porque Flutter las captura así del GPS
  @IsNumber({}, { message: 'La latitud debe ser un número decimal' })
  @Min(-90, { message: 'Latitud inválida' })
  @Max(90, { message: 'Latitud inválida' })
  @IsNotEmpty({ message: 'La latitud es obligatoria' })
  latitud!: number;

  @IsNumber({}, { message: 'La longitud debe ser un número decimal' })
  @Min(-180, { message: 'Longitud inválida' })
  @Max(180, { message: 'Longitud inválida' })
  @IsNotEmpty({ message: 'La longitud es obligatoria' })
  longitud!: number;
}