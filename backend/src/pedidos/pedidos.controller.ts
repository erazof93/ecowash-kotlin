import { Controller, Post, Get, Body, UseGuards, Req, Query } from '@nestjs/common';
import { PedidosService } from './pedidos.service';
import { CreatePedidoDto } from './dto/create-pedido.dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { GetCercanosDto } from './dto/get-cercanos.dto';

@Controller('pedidos')
@UseGuards(JwtAuthGuard) // 🔒 Protegemos todas las rutas de pedidos con JWT
export class PedidosController {
  constructor(private readonly pedidosService: PedidosService) {}

  @Post()
  async crear(@Req() req: any, @Body() createPedidoDto: CreatePedidoDto) {
    // Extraemos el ID del cliente que viaja encriptado en el Token JWT (payload.sub)
    const clienteId = req.user.id; 
    console.log("👉 EL ID QUE VIENE EN EL TOKEN ES:", req.user.id);
    return this.pedidosService.crearPedido(clienteId, createPedidoDto);
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