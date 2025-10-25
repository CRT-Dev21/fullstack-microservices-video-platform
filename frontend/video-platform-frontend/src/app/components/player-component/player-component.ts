import { CommonModule } from '@angular/common';
import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DomSanitizer, SafeResourceUrl, SafeUrl } from '@angular/platform-browser';
import { Router, RouterModule } from '@angular/router';
import Hls from 'hls.js';
import { Observable, BehaviorSubject, switchMap, catchError, EMPTY, scan } from 'rxjs'; 
import { VideoService } from '../../core/services/video-service';
import { EnrichedVideoMetadata, EnrichedVideosResponse } from '../../core/models/video.models';
import { buildSafeUrl } from '../../core/utils/safeUrl-util';

interface VideoUrls {
  [quality: string]: string;
}

@Component({
  selector: 'app-player-component',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './player-component.html',
  styleUrl: './player-component.css'
})
export class PlayerComponent implements OnInit, OnDestroy {
  @ViewChild('videoPlayer') videoPlayer!: ElementRef<HTMLVideoElement>;

  creatorId!: string;
  creatorName!: string;
  creatorAvatarUrl!: SafeResourceUrl;
  videoUrl!: SafeUrl;
  originalUrls!: VideoUrls;
  currentQuality = '360p';
  title = '';
  description = '';
  showSettingsMenu = false;
  availableQualities: string[] = [];
  videoId!: string;

  relatedVideos$!: Observable<EnrichedVideoMetadata[]>;
  pageSubject = new BehaviorSubject<number>(0);
  isLoadingRelated = false;
  relatedError = '';
  relatedTotalPages = 0;
  
  private readonly imageBase = 'http://localhost:8080/api/v1/catalog/images';
  private readonly avatarBase = 'http://localhost:8080/api/v1/users/avatars';
  private hls!: Hls;

  constructor(
    private router: Router, 
    private sanitizer: DomSanitizer,
    private videoService: VideoService
) {}

ngOnInit() {
  const state = history.state;
  console.log('STATE RECIBIDO EN PLAYER:', state);
  if (!state.videoUrls) {
    this.router.navigate(['/']);
    return;
  }

  this.videoId = state.videoId;
  this.creatorId = state.creator?.id;
  this.creatorName = state.creator?.name;

  const rawAvatarUrl = state.creator?.avatarUrl as string | undefined;
  if (rawAvatarUrl) {
    this.creatorAvatarUrl = this.sanitizer.bypassSecurityTrustResourceUrl(rawAvatarUrl);
  }

  this.originalUrls = state.videoUrls;
  this.title = state.title;
  this.description = state.description;

  this.availableQualities = Object.keys(this.originalUrls)
    .sort((a, b) => {
        const numA = parseInt(a.replace('p', ''));
        const numB = parseInt(b.replace('p', ''));
        return numA + numB;
      });

    if (!this.availableQualities.includes(this.currentQuality) && this.availableQualities.length > 0) {
      this.currentQuality = this.availableQualities[0]; 
    }
    
    if (this.title && this.videoId) {
      this.loadRelatedVideos(this.title, this.videoId);
    }
  }

  ngOnDestroy() {
    if (this.hls) {
      this.hls.destroy();
    }
  }

  ngAfterViewInit() {
    this.setQuality(this.currentQuality, 0);
  }

  setQuality(quality: string, startTime: number = 0) {
  if (!this.originalUrls[quality]) return;
  this.currentQuality = quality;

  const url = `http://localhost:8080/api/v1/stream?path=${encodeURIComponent(this.originalUrls[quality])}`;
  const token = localStorage.getItem('jwt_token');

  if (Hls.isSupported()) {
    const hls = new Hls({
      xhrSetup: (xhr, url) => {
        if (token) {
        xhr.setRequestHeader('Authorization', `Bearer ${token}`);
        }
      }
    });

    hls.loadSource(url);
    hls.attachMedia(this.videoPlayer.nativeElement);

    hls.on(Hls.Events.MANIFEST_PARSED, () => {
      this.videoPlayer.nativeElement.currentTime = startTime;
      this.videoPlayer.nativeElement.play();
    });

    hls.on(Hls.Events.ERROR, (event, data) => {
      if (data.fatal) {
        console.error('FATAL HLS error:', data);
      }
  });

  } else if (this.videoPlayer.nativeElement.canPlayType('application/vnd.apple.mpegurl')) {
    this.videoPlayer.nativeElement.src = url;
    this.videoPlayer.nativeElement.currentTime = startTime;
    this.videoPlayer.nativeElement.play();
    }
  }

  toggleSettingsMenu() {
    this.showSettingsMenu = !this.showSettingsMenu;
  }

  onQualityChange() {
    const currentTime = this.videoPlayer.nativeElement.currentTime;
    this.setQuality(this.currentQuality, currentTime);
    this.showSettingsMenu = false;
  }

  goToUpload(){
    this.router.navigate(['/dashboard'])
  }

  loadRelatedVideos(title: string, currentVideoId: string) {
    this.pageSubject.next(0);
    this.relatedVideos$ = this.pageSubject.pipe(
      switchMap(page => {
      if (page === -1) return EMPTY; 
        this.isLoadingRelated = true;
        return this.videoService.getRelatedVideos(title, currentVideoId, 10).pipe(
        catchError(err => {
          this.isLoadingRelated = false;
          this.relatedError = 'Error al cargar videos relacionados.';
          console.error(err);
          return EMPTY;
        })
        );
      }),
      scan((acc: EnrichedVideoMetadata[], res: EnrichedVideosResponse) => {
      this.isLoadingRelated = false;
        this.relatedTotalPages = res.totalPages;
      const enriched = res.content.map(v => ({
        ...v,
        safeThumbnailUrl: buildSafeUrl(this.sanitizer, this.imageBase, v.thumbnailUrl),
        safeCreatorAvatarUrl: buildSafeUrl(this.sanitizer, this.avatarBase, v.creator.avatarUrl) 
      }));
        const filtered = enriched.filter(v => v.videoId !== currentVideoId);

        return res.page === 0 ? filtered : [...acc, ...filtered];
      }, [])
    );
  }

  loadNextRelatedPage() {
    const currentPage = this.pageSubject.value;
    if (currentPage < this.relatedTotalPages - 1 && !this.isLoadingRelated) {
      this.pageSubject.next(currentPage + 1);
    }
  }

  onScroll(event: Event) {
    const target = event.target as HTMLElement;
    const isNearBottom = target.scrollTop + target.clientHeight >= target.scrollHeight - 100;
    if (isNearBottom && !this.isLoadingRelated) {
      this.loadNextRelatedPage();
    }
   }

  playVideo(video: EnrichedVideoMetadata) {
      this.router.navigate(['/player'], {
      state: {
          videoId: video.videoId,
          videoUrls: video.videoUrls,
          title: video.title,
          description: video.description,
          creator: {
            id: video.creator.creatorId,
            name: video.creator.username,
            avatarUrl: (video.safeCreatorAvatarUrl as any).changingThisBreaksApplicationSecurity
          }
        }
      });
  }
}
