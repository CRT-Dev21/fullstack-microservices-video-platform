import { SafeResourceUrl } from "@angular/platform-browser";

export interface CreatorDetails {
    creatorId: string;
    username: string;
    avatarUrl: string;
}

export interface EnrichedVideoMetadata extends VideoMetadata {
  creator: CreatorDetails;
  safeThumbnailUrl?: SafeResourceUrl;
  safeCreatorAvatarUrl?: SafeResourceUrl;
}


export interface EnrichedVideosResponse {
    content: EnrichedVideoMetadata[]; 
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}

export interface VideoMetadata {
  videoId: string;
  creatorId: string;
  title: string;
  description: string;
  thumbnailUrl: string;
  videoUrls: Record<string, string>;
  duration: string;
  status: string;
}

export interface VideosResponse {
  content: VideoMetadata[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}