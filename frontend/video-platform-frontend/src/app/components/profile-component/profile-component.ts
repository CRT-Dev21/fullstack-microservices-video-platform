import { CommonModule } from '@angular/common';
import { Component, HostListener, OnInit } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { HttpClient } from '@angular/common/http';
import { VideoMetadata, VideosResponse } from '../../core/models/video.models';
import { catchError, EMPTY, switchMap } from 'rxjs';
import { VideoService } from '../../core/services/video-service';
import { Router } from '@angular/router';
import { UserService } from '../../core/services/user-service';
import { buildSafeUrl } from '../../core/utils/safeUrl-util';
import { TruncatePipe } from '../../core/utils/truncate-util';

interface SafeVideo extends VideoMetadata {
  safeThumbnailUrl: SafeResourceUrl;
}

@Component({
  selector: 'app-profile-component',
  standalone: true,
  imports: [CommonModule, TruncatePipe],
  templateUrl: './profile-component.html',
  styleUrl: './profile-component.css'
})
export class ProfileComponent implements OnInit {
  username = '';
  avatarUrl = '';
  safeAvatarUrl!: SafeResourceUrl;

  videos: SafeVideo[] = [];
  page = 0;
  totalPages = 0;
  isLoading = false;

  private readonly imageBase = 'http://localhost:8080/api/v1/catalog/images';
  private readonly avatarBase = 'http://localhost:8080/api/v1/users/avatars';

  constructor(
    private http: HttpClient,
    private sanitizer: DomSanitizer,
    private videoService: VideoService,
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit() {
    this.userService.user$.subscribe(user => {
      this.username = user?.username || '';
      this.avatarUrl = user?.avatarUrl || '';
      this.safeAvatarUrl = buildSafeUrl(this.sanitizer, this.avatarBase, user?.avatarUrl ?? null);
    });

    if (this.userService.currentUser?.username !== this.username) {
      this.userService.loadUser().subscribe();
    }

    this.loadUserVideos();
  }

  loadUserVideos() {
    if (this.isLoading || (this.totalPages && this.page >= this.totalPages)) return;
    this.isLoading = true;

    this.videoService.getMyVideos(this.page, 8)
      .pipe(catchError(() => {
        this.isLoading = false;
        return EMPTY;
      }))
      .subscribe({
        next: (res: VideosResponse) => {
          const enriched = res.content.map(video => ({
            ...video,
            safeThumbnailUrl: buildSafeUrl(this.sanitizer, this.imageBase, video.thumbnailUrl)
          }));
          this.videos.push(...enriched);
          this.totalPages = res.totalPages;
          this.isLoading = false;
        }
      });
  }

  onAvatarChange(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;

    const file = input.files[0];
    const formData = new FormData();
    formData.append('avatar', file);

    this.http.post<{ avatarUrl: string }>('http://localhost:8080/api/v1/upload/avatars', formData)
      .pipe(
        switchMap(uploadResponse => {
          const avatarUrl = uploadResponse.avatarUrl;
          return this.http.put('http://localhost:8080/api/v1/users/avatars', { avatarUrl })
            .pipe(switchMap(() => this.http.get<{ username: string; avatarUrl: string }>('http://localhost:8080/api/v1/users/me')));
        })
      )
      .subscribe({
        next: (res) => {
          this.username = res.username;
          this.avatarUrl = res.avatarUrl;
          this.safeAvatarUrl = buildSafeUrl(this.sanitizer, this.avatarBase, res.avatarUrl);
          this.userService.updateAvatar(res.avatarUrl);
        },
        error: (err) => console.error('Error al cambiar avatar:', err)
      });
  }

  @HostListener('window:scroll', [])
  onScroll() {
    const nearBottom = window.innerHeight + window.scrollY >= document.body.scrollHeight - 200;
    if (nearBottom && !this.isLoading) {
      this.page++;
      this.loadUserVideos();
    }
  }

  playVideo(video: SafeVideo) {
    this.router.navigate(['/player'], {
      state: {
        videoId: video.videoId,
        videoUrls: video.videoUrls,
        title: video.title,
        description: video.description
      }
    });
  }
}
