import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { BehaviorSubject, catchError, EMPTY, Observable, scan, switchMap } from 'rxjs';
import { VideoService } from '../../core/services/video-service';
import { EnrichedVideoMetadata, EnrichedVideosResponse } from '../../core/models/video.models';
import { DomSanitizer } from '@angular/platform-browser';
import { buildSafeUrl } from '../../core/utils/safeUrl-util';

@Component({
  selector: 'app-home-component',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './home-component.html',
  styleUrl: './home-component.css'
})
export class HomeComponent implements OnInit {
  pageSubject = new BehaviorSubject<number>(0);
  videos$!: Observable<EnrichedVideoMetadata[]>;
  error = '';
  totalPages = 0;
  isLoading = false;
  searchQuery = '';

  private readonly imageBase = 'http://localhost:8080/api/v1/catalog/images';
  private readonly avatarBase = 'http://localhost:8080/api/v1/users/avatars';

  constructor(
    private videoService: VideoService,
    private router: Router,
    private sanitizer: DomSanitizer,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      const query = params['q'];
      if (query) {
        this.onSearch(query);
      } else {
        this.setupInfiniteScroll();
      }
    });
  }

  onSearch(query: string) {
    this.searchQuery = query;
    this.pageSubject.next(0);
    this.videos$ = this.pageSubject.pipe(
      switchMap(page => {
        this.isLoading = true;
        const request$ = query
          ? this.videoService.searchVideos(query, page, 10)
          : this.videoService.getVideosPage(page, 10);
        return request$.pipe(
          catchError(() => {
            this.error = 'Error al cargar videos';
            this.isLoading = false;
            return EMPTY;
          })
        );
      }),
      scan((acc: EnrichedVideoMetadata[], res: EnrichedVideosResponse) => {
        this.isLoading = false;
        this.totalPages = res.totalPages;
        const enriched = res.content.map(v => ({
          ...v,
          safeThumbnailUrl: buildSafeUrl(this.sanitizer, this.imageBase, v.thumbnailUrl),
          safeCreatorAvatarUrl: buildSafeUrl(this.sanitizer, this.avatarBase, v.creator.avatarUrl)
        }));
        return res.page === 0 ? enriched : [...acc, ...enriched];
      }, [])
    );
  }

  setupInfiniteScroll() {
    this.videos$ = this.pageSubject.pipe(
      switchMap(page => {
        if (page === -1) return EMPTY;
        this.isLoading = true;
        return this.videoService.getVideosPage(page, 10).pipe(
          catchError(err => {
            this.isLoading = false;
            this.error = 'Error de conexión o datos.';
            console.error(err);
            return EMPTY;
          })
        );
      }),
      scan((acc: EnrichedVideoMetadata[], res: EnrichedVideosResponse) => {
        this.isLoading = false;
        this.totalPages = res.totalPages;
        const enriched = res.content.map(v => ({
          ...v,
          safeThumbnailUrl: buildSafeUrl(this.sanitizer, this.imageBase, v.thumbnailUrl),
          safeCreatorAvatarUrl: buildSafeUrl(this.sanitizer, this.avatarBase, v.creator.avatarUrl)
        }));
        return res.page === 0 ? enriched : [...acc, ...enriched];
      }, [])
    );
  }

  onScroll(event: Event) {
    const target = event.target as HTMLElement;
    const isNearBottom = target.scrollTop + target.clientHeight >= target.scrollHeight - 100;
    if (isNearBottom && !this.isLoading) {
      this.loadNextPage();
    }
  }

  loadNextPage() {
    const currentPage = this.pageSubject.value;
    if (currentPage < this.totalPages - 1 && !this.isLoading) {
      this.pageSubject.next(currentPage + 1);
    }
  }

  playVideo(video: EnrichedVideoMetadata) {
    console.log('Video completo antes de navegar:', video);
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
