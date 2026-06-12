import { Controller, Post, Get, Patch, Param, Body, UseGuards, Req, Query } from '@nestjs/common';
import { PedidosService } from './pedidos.service';
import { CreatePedidoDto } from './dto/create-pedido.dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { GetCercanosDto } from './dto/get-cercanos.dto';

@Controller('pedidos')
@UseGuards(JwtAuthGuard)
export class PedidosController {
  constructor(private readonly pedidosService: PedidosService) {}

  @Post()
  async crear(@Req() req: any, @Body() createPedidoDto: CreatePedidoDto) {
    const clienteId = req.user.id;
    console.log("👉 EL ID QUE VIENE EN EL TOKEN ES:", req.user.id);
    return this.pedidosService.crearPedido(clienteId, createPedidoDto);
  }

  @Patch(':id/cancelar')
  async cancelar(@Param('id') id: string, @Req() req: any) {
    return this.pedidosService.cancelarPedido(id, req.user.id);
  }

  @Get()
  async obtenerTodos() {
    return this.pedidosService.obtenerTodos();
  }

  @Get('cercanos')
  async obtenerCercanos(@Query() query: GetCercanosDto) {
    return this.pedidosService.obtenerPedidosCercanos(query);
  }
}

  @Get()
  async obtenerTodos() {
    return this.pedidosService.obtenerTodos();
  }

  @Get('cercanos')
async obtenerCercanos(@Query() query: GetCercanosDto) {
  return this.pedidosService.obtenerPedidosCercanos(query);
}
}