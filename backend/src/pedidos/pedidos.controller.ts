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
    return this.pedidosService.crearPedido(clienteId, createPedidoDto);
  }

  @Patch(':id/cancelar')
  async cancelar(@Param('id') id: string, @Req() req: any) {
    return this.pedidosService.cancelarPedido(id, req.user.id);
  }

  @Patch(':id/aceptar')
  async aceptar(@Param('id') id: string, @Req() req: any) {
    return this.pedidosService.aceptarPedido(id, req.user.id);
  }

  @Patch(':id/en-camino')
  async enCamino(@Param('id') id: string, @Req() req: any) {
    return this.pedidosService.cambiarEstado(id, req.user.id, 'ACEPTADO', 'EN_CAMINO');
  }

  @Patch(':id/en-sitio')
  async enSitio(@Param('id') id: string, @Req() req: any) {
    return this.pedidosService.cambiarEstado(id, req.user.id, 'EN_CAMINO', 'EN_SITIO');
  }

  @Patch(':id/iniciar-lavado')
  async iniciarLavado(@Param('id') id: string, @Req() req: any) {
    return this.pedidosService.cambiarEstado(id, req.user.id, 'EN_SITIO', 'LAVANDO');
  }

  @Patch(':id/finalizar')
  async finalizar(@Param('id') id: string, @Req() req: any) {
    return this.pedidosService.cambiarEstado(id, req.user.id, 'LAVANDO', 'FINALIZADO');
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
