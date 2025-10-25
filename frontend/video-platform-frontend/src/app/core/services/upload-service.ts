import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UploadService {
  private apiUrl = 'http://localhost:8080/api/v1/upload';
  constructor(private http: HttpClient){}

  uploadVideo(
    title: string,
    description: string,
    imageFile: File,
    videoFile: File,
  ): Observable<any> {
    const formData = new FormData();

    formData.append('title', title);
    formData.append('description', description);
    formData.append('image', imageFile);
    formData.append('video', videoFile);

    return this.http.post(`${this.apiUrl}`, formData);
  }

  uploadAvatar(imageFile: File): Observable<{avatarUrl: string}> {
    const formData = new FormData();

    formData.append('image', imageFile);

    return this.http.post<{avatarUrl: string}>(`${this.apiUrl}/avatars`, formData);
  }

}