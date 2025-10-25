import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UploadService } from '../../core/services/upload-service';

@Component({
  selector: 'app-dashboard-component',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard-component.html',
  styleUrl: './dashboard-component.css'
})
export class DashboardComponent {
  title = '';
  description = '';
  imageFile: File | null = null;
  videoFile: File | null = null;
  notification: { type: 'success' | 'error'; message: string } | null = null;
  isDraggingImage = false;
  isDraggingVideo = false;

  constructor(
    private uploaderService: UploadService
  ) {}

  selectFile(type: 'image' | 'video') {
    const input = document.getElementById(
      type === 'image' ? 'imageInput' : 'videoInput'
    ) as HTMLInputElement | null;
    input?.click();
  }

  onFileSelect(event: Event, type: 'image' | 'video') {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const file = input.files[0];
    if (type === 'image') this.imageFile = file;
    else this.videoFile = file;
  }

  uploadVideo() {
    if (!this.title || !this.description || !this.imageFile || !this.videoFile) {
      this.showNotification('error', 'Completa todos los campos antes de subir.');
      return;
    }

    this.uploaderService
      .uploadVideo(this.title, this.description, this.imageFile, this.videoFile)
      .subscribe({
        next: () => this.showNotification('success', 'Upload successful! Your video is now being processed, and we will notify you when it is ready.'),
        error: () =>
          this.showNotification('error', 'Error uploading video, try again later.')
      });
  }

  onDragOver(event: DragEvent, type: 'image' | 'video') {
    event.preventDefault();
    if (type === 'image') this.isDraggingImage = true;
    else this.isDraggingVideo = true;
  }

  onDragLeave(type: 'image' | 'video') {
    if (type === 'image') this.isDraggingImage = false;
    else this.isDraggingVideo = false;
  }

  onDrop(event: DragEvent, type: 'image' | 'video') {
    event.preventDefault();
    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      const file = files[0];
      if (type === 'image') {
        this.imageFile = file;
        this.isDraggingImage = false;
      } else {
        this.videoFile = file;
        this.isDraggingVideo = false;
      }
    }
  }

  showNotification(type: 'success' | 'error', message: string) {
    this.notification = { type, message };
    setTimeout(() => (this.notification = null), 5000);
  }
}
