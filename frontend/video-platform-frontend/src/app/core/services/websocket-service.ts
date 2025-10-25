import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

interface NotificationMessage {
  status: 'SUCCESS' | 'FAILED';
  message: string;
}

@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private ws: WebSocket | null = null;
  private notificationSubject = new BehaviorSubject<NotificationMessage | null>(null);
  notifications$ = this.notificationSubject.asObservable();

  connect() {
    const token = localStorage.getItem('jwt_token');
    if (!token) {
      return;
    }

    if (this.ws && this.ws.readyState === WebSocket.OPEN) return;

    const wsUrl = `ws://localhost:8080/notifications?token=${token}`;

    this.ws = new WebSocket(wsUrl);

    this.ws.onopen = () => {};
    this.ws.onclose = (e) => {};
    this.ws.onerror = (e) => {};
    this.ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        this.notificationSubject.next(data);
      } catch (err) {
        console.error('Invalid WS message:', err); 
      }
    };
  }

  disconnect() {
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }
}