import { Controller, Get, Param, Query } from '@nestjs/common';
import { PreciosService } from './precios.service';


@Controller('precios') // Prefijo global: http://localhost:3000/precios
export class PreciosController {
  constructor(private readonly preciosService: PreciosService) {}

  // GET /precios -> Devuelve toda la matriz cruzada de tarifas
  @Get()
  async listarMatriz() {
    return this.preciosService.obtenerMatrizPrecios();
  }

  // GET /precios/categorias -> Devuelve solo la lista de tamaños ('Auto', 'Camioneta', etc.)
  @Get('categorias')
  async listarCategorias() {
    return this.preciosService.obtenerCategorias();
  }
}
