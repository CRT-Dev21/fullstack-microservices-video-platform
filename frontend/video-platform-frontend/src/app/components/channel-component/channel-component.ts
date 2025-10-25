import { CommonModule } from '@angular/common';
import { Component, HostListener, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { HttpClient } from '@angular/common/http';
import { VideoService } from '../../core/services/video-service';
import { VideoMetadata, VideosResponse } from '../../core/models/video.models';
import { catchError, EMPTY } from 'rxjs';
import { buildSafeUrl } from '../../core/utils/safeUrl-util';
import { TruncatePipe } from '../../core/utils/truncate-util';

interface SafeVideo extends VideoMetadata {
  safeThumbnailUrl: SafeResourceUrl;
}

@Component({
  selector: 'app-channel-component',
  standalone: true,
  imports: [CommonModule, TruncatePipe],
  templateUrl: './channel-component.html',
  styleUrl: './channel-component.css'
})
export class ChannelComponent implements OnInit {
  username = '';
  safeAvatarUrl!: SafeResourceUrl;
  videos: SafeVideo[] = [];
  page = 0;
  totalPages = 0;
  isLoading = false;
  creatorId = '';

  private readonly imageBase = 'http://localhost:8080/api/v1/catalog/images';
  private readonly avatarBase = 'http://localhost:8080/api/v1/users/avatars';

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private videoService: VideoService,
    private sanitizer: DomSanitizer,
    private router: Router
  ) {}

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const newCreatorId = params.get('id') || '';
      
      if (this.creatorId !== newCreatorId) {
          this.creatorId = newCreatorId;
          this.videos = [];
          this.page = 0;
          this.totalPages = 0;
          this.isLoading = false;
          
          if (this.creatorId) {
            this.loadChannelData();
            this.loadCreatorVideos(); 
          }
      }
    });
  }

  loadChannelData() {
    this.http
      .get<{ username: string; avatarUrl: string }>(
        `http://localhost:8080/api/v1/users/${this.creatorId}`
      )
      .subscribe({
        next: res => {
          this.username = res.username;
          this.safeAvatarUrl = buildSafeUrl(this.sanitizer, this.avatarBase, res.avatarUrl);
        },
        error: err => console.error('Error al cargar canal:', err)
      });
  }

  loadCreatorVideos() {
    if (this.isLoading || (this.totalPages && this.page >= this.totalPages)) return;
    this.isLoading = true;

    this.videoService
      .getVideosByCreatorId(this.creatorId, this.page, 8)
      .pipe(catchError(() => {
        this.isLoading = false;
        return EMPTY;
      }))
      .subscribe({
        next: (res: VideosResponse) => {
          const enriched = res.content.map(v => ({
            ...v,
            safeThumbnailUrl: buildSafeUrl(this.sanitizer, this.imageBase, v.thumbnailUrl)
          }));
          this.videos.push(...enriched);
          this.totalPages = res.totalPages;
          this.isLoading = false;
        }
      });
  }

  @HostListener('window:scroll', [])
  onScroll() {
    const nearBottom = window.innerHeight + window.scrollY >= document.body.scrollHeight - 200;
    if (nearBottom && !this.isLoading) {
      this.page++;
      this.loadCreatorVideos();
    }
  }

  playVideo(video: VideoMetadata) {
    this.router.navigate(['/player'], {
      state: {
        video: video.videoId,
        videoUrls: video.videoUrls,
        title: video.title,
        description: video.description,
        creator: {
          id: this.creatorId,
          name: this.username,
          avatarUrl: (this.safeAvatarUrl as any).changingThisBreaksApplicationSecurity
        }
      }
    });
  }
}
