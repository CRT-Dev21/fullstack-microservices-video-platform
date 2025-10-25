import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import {EnrichedVideosResponse, VideosResponse } from "../models/video.models";
import { Observable } from "rxjs";

@Injectable({
    providedIn: 'root'
}) 
export class VideoService {
    private catalogServiceurl = 'http://localhost:8080/api/v1/catalog/videos'
    private enrichedVideosApiUrl = 'http://localhost:8080/api/v1/catalog/enriched-videos';
    private enrichedRelatedVideos = 'http://localhost:8080/api/v1/catalog/enriched-related-videos';

    constructor(private http: HttpClient) {}

    getVideosPage(page: number, size: number): Observable<EnrichedVideosResponse> {
        return this.http.get<EnrichedVideosResponse>(`${this.enrichedVideosApiUrl}?page=${page}&?size=${size}`);
    }

    getRelatedVideos(title: string, currentVideoId: string, limit: number): Observable<EnrichedVideosResponse> {
        return this.http.get<EnrichedVideosResponse>(`${this.enrichedRelatedVideos}?title=${title}&currentVideoId=${currentVideoId}&limit=${limit}`);
    }

    searchVideos(query: string, page: number, size: number): Observable<EnrichedVideosResponse> {
        return this.http.get<EnrichedVideosResponse>(`${this.enrichedVideosApiUrl}?query=${query}&page=${page}&size=${size}`);
    }

    getMyVideos(page: number, size: number): Observable<VideosResponse>{
        return this.http.get<VideosResponse>(`${this.catalogServiceurl}/me?page=${page}&?size=${size}`);
    }

    getVideosByCreatorId(creatorId: string, page: number, size: number): Observable<VideosResponse>{
        return this.http.get<VideosResponse>(`${this.catalogServiceurl}/${creatorId}?page=${page}&?size=${size}`);
    }
}