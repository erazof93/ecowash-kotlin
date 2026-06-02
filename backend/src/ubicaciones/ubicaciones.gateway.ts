import { 
  WebSocketGateway, 
  WebSocketServer, 
  SubscribeMessage, 
  OnGatewayConnection, 
  OnGatewayDisconnect,
  ConnectedSocket,
  MessageBody
} from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';
import { UbicacionesService } from './ubicaciones.service';
import { UseFilters, UsePipes, ValidationPipe } from '@nestjs/common';
import { OnEvent } from '@nestjs/event-emitter';
import { PrismaService } from 'src/prisma/prisma.service';

// 🚀 Configuramos el Gateway permitiendo CORS para que tu app de Flutter conecte sin problemas
@WebSocketGateway({
  cors: {
    origin: '*',
  },
  namespace: 'ubicaciones', // Separamos este canal bajo el namespace /ubicaciones
})
export class UbicacionesGateway implements OnGatewayConnection, OnGatewayDisconnect {

  @OnEvent('pedido.creado')
  async handlePedidoCreadoNotificacion(payload: { pedido: any; latitud: number; longitud: number }) {
    console.log(`📣 Evento interno recibido: Analizando radar para el pedido ${payload.pedido.id}`);

    // 🗺️ Buscamos los lavadores que estén en línea a menos de 5000 metros (5KM) de la latitud/longitud del pedido
    const radioMetros = 5000; 
    
    try {
      // 🎯 Corrección: Se usan las propiedades directas del payload en español
      const lavadoresCercanos: any[] = await this.prisma.$queryRaw`
        SELECT 
          lavador_id,
          ST_Distance(coordenada, ST_SetSRID(ST_MakePoint(${Number(payload.longitud)}, ${Number(payload.latitud)}), 4326)::geography) as distancia
        FROM ubicaciones_lavadores
        WHERE ST_DWithin(coordenada, ST_SetSRID(ST_MakePoint(${Number(payload.longitud)}, ${Number(payload.latitud)}), 4326)::geography, ${radioMetros})
        AND ultima_actualizacion >= NOW() - INTERVAL '5 minutes'; -- ⏱️ Solo lavadores activos en los últimos 5 min
      `;

      console.log(`🎯 Se encontraron ${lavadoresCercanos.length} lavadores en el rango de cobertura.`);

      // 📡 Emitimos la alerta por WebSocket a todo el namespace bajo el evento 'nuevo_pedido_disponible'
      if (lavadoresCercanos.length > 0) {
        this.server.emit('nuevo_pedido_disponible', {
          mensaje: '¡Hay un nuevo lavado cerca de tu ubicación!',
          pedido: payload.pedido,
          coordenadas_cliente: {
            latitud: payload.latitud,
            longitud: payload.longitud
          }
        });
        console.log(`⚡ Alerta de WebSocket enviada a los lavadores en zona.`);
      }

    } catch (error) {
      console.error('Error al calcular radar de lavadores para notificación:', error);
    }
  }
  
  @WebSocketServer()
  server!: Server;

  constructor(private readonly ubicacionesService: UbicacionesService, private readonly prisma: PrismaService) {}

  // 🔌 Se ejecuta automáticamente cuando un dispositivo abre el mapa en Flutter
  handleConnection(client: Socket) {
    console.log(`🔌 Dispositivo conectado a WebSockets: ${client.id}`);
  }

  // ❌ Se ejecuta cuando el lavador cierra la app o pierde señal
  handleDisconnect(client: Socket) {
    console.log(`❌ Dispositivo desconectado: ${client.id}`);
  }

  // 🛰️ Evento: 'actualizar_ubicacion'
  // Escucha las coordenadas constantes enviadas en segundo plano por el lavador
  @SubscribeMessage('actualizar_ubicacion')
  async handleActualizarUbicacion(
    @ConnectedSocket() client: Socket,
    @MessageBody() payload: any
  ) {
    let data = payload;

    // 🧼 Bucle de limpieza: Mientras los datos lleguen envueltos como string, los desempaquetamos
    while (typeof data === 'string') {
      try {
        // Quitamos posibles comillas flojas al inicio y al final que rompen el JSON estándar
        const stringLimpio = data.trim().replace(/^['"\s]+|['"\s]+$/g, '');
        
        // Si después de limpiar ya no parece un objeto JSON, rompemos el bucle
        if (!stringLimpio.startsWith('{')) {
          data = stringLimpio;
          break;
        }
        
        data = JSON.parse(stringLimpio);
      } catch (e) {
        console.error('Error al intentar parsear el string:', data);
        client.emit('error_nodo', { message: 'El formato JSON enviado es inválido' });
        return;
      }
    }

    // 🎯 Validación estricta de las propiedades requeridas
    if (!data || typeof data !== 'object' || !data.lavador_id || !data.latitud || !data.longitud) {
      client.emit('error_nodo', { message: 'Datos de ubicación incompletos o inválidos' });
      return;
    }

    try {
      // 💾 Guardamos de forma segura la coordenada en PostgreSQL vía PostGIS
      await this.ubicacionesService.guardarUbicacionNativa(
        data.lavador_id,
        Number(data.latitud),
        Number(data.longitud)
      );

      // 🏁 Notificamos éxito absoluto al cliente
      client.emit('ubicacion_recibida', { status: 'OK', ts: new Date() });
      console.log(`🛰️ Ubicación del Lavador ${data.lavador_id} guardada en base de datos con éxito.`);

    } catch (error) {
      console.error('Error al ejecutar query geoespacial:', error);
      client.emit('error_nodo', { message: 'Error interno al guardar en PostGIS' });
    }
  }
}