import { Injectable } from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {User , LoginUser} from '../../model/User';
import {PendingHost} from '../../model/PendingHost';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UserService {


  private registrationUrl = 'https://localhost:8443/Registration';
  private usersUrl = 'https://localhost:8443/Users';
  private pendingHostsUrl = 'https://localhost:8443/PendingHosts';
  private LoginUrl = 'https://localhost:8443/login';
  private ImageUrl = 'https://localhost:8443/Users/{id}/Image';

  constructor(private http: HttpClient) {
  }

  register(user: User, host: boolean): Observable<User> {
    return this.http.post<User>(this.registrationUrl, user);
  }

  getUsers(): Observable<User[]>{
    return this.http.get<User[]>(this.usersUrl);
  }

  getUser(id: string): Observable<User>{
    return this.http.get<User>(this.usersUrl + '/' + id);
  }

  updateUser(user: User, id: number): void{
    this.http.put<any>(this.usersUrl + '/' + id, user).subscribe();
  }

  getPendingHost(id: string): Observable<PendingHost>{
    return this.http.get<PendingHost>(this.pendingHostsUrl + '/' + id);
  }

  uploadPendingHost(pendingHost: number): void{
    this.http.post<any>(this.pendingHostsUrl, pendingHost).subscribe();
  }

  deletePendingHost(pendingHost: number): void{
    this.http.delete(this.pendingHostsUrl + '/' + pendingHost).subscribe();
  }

  LoginRequest(userName: string, password: string): Observable<HttpResponse<string>>{
    const json: LoginUser = { userName, password };
    return this.http.post<string>(this.LoginUrl, json, { observe: 'response'});
  }

  Logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
  }

  LoggedIn(): boolean{
    return localStorage.getItem('token') != null;
  }

  UploadImage(Image: File): number{
    console.log(this.http.post<File>(this.ImageUrl, Image) );
    return 0;
  }
}
