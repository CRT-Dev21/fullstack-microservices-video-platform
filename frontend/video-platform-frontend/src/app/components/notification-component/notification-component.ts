import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-notification-component',
  imports: [CommonModule],
  templateUrl: './notification-component.html',
  styleUrl: './notification-component.css'
})
export class NotificationComponent {
  @Input() message = '';
  @Input() type: 'SUCCESS' | 'FAILED' = 'SUCCESS';
}
