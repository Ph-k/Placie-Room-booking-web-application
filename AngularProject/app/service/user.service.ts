import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders, HttpResponse} from '@angular/common/http';
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
  private RootUrl = 'https://localhost:8443';

  constructor(private http: HttpClient) { }

  authorizationHeader(): { headers: { Authorization: string } }{
    return { headers: {Authorization: localStorage.getItem('token') }  };
  }

  register(user: User, host: boolean): Observable<User> {
    return this.http.post<User>(this.registrationUrl, user);
  }

  getUsers(): Observable<User[]>{
    return this.http.get<User[]>(this.usersUrl, this.authorizationHeader());
  }

  getUsersxml(): Observable<string>{
    const httpHeader: HttpHeaders = new HttpHeaders({
      Accept: 'application/xml',
      Authorization: localStorage.getItem('token')
    });
    return this.http.get(this.usersUrl, { headers: httpHeader , responseType: 'text'});
  }

  getUser(id: string): Observable<User>{
    // console.log('Here');
    // console.log(this.authorizationHeader);
    if(localStorage.getItem('token') == null){return this.http.get<User>(this.usersUrl + '/' + id) ; }
    return this.http.get<User>(this.usersUrl + '/' + id, this.authorizationHeader());
  }

  async getUserId( username: string): Promise<number>{
    const response = await this.http.get<number>(this.RootUrl + '/UserId/' + username, this.authorizationHeader()).toPromise();
    return response;
  }

  findUserId( username: string): Observable<number>{
    return this.http.get<number>(this.RootUrl + '/UserId/' + username, this.authorizationHeader());
  }

  updateUser(user: User, id: number): void{
    this.http.put<any>(this.usersUrl + '/' + id, user, this.authorizationHeader()).subscribe();
  }

  updateUserPassword(user: User, id: number): void{
    this.http.put<any>(this.usersUrl + 'NewPassword/' + id, user, this.authorizationHeader()).subscribe();
  }

  getPendingHost(id: string): Observable<PendingHost>{
    return this.http.get<PendingHost>(this.pendingHostsUrl + '/' + id, this.authorizationHeader());
  }

  getPendingHosts(): Observable<PendingHost[]>{
    return this.http.get<PendingHost[]>(this.pendingHostsUrl , this.authorizationHeader());
  }

  getPendingHostsXml(): Observable<string>{
    const httpHeader: HttpHeaders = new HttpHeaders({
      Accept: 'application/xml',
      Authorization: localStorage.getItem('token')
    });
    return this.http.get(this.pendingHostsUrl, { headers: httpHeader , responseType: 'text'});
  }

  uploadPendingHost(pendingHost: number): void{
    this.http.post<any>(this.pendingHostsUrl, pendingHost, this.authorizationHeader()).subscribe();
  }

  deletePendingHost(pendingHost: number): void{
    this.http.delete(this.pendingHostsUrl + '/' + pendingHost, this.authorizationHeader()).subscribe();
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

  UploadImage(username: string, Image: File): Observable<any>{
    const formdata  = new FormData();
    formdata.append('file', Image, Image.name);

    return this.http.post<any>(this.RootUrl + '/Users/Image/' + username, formdata, this.authorizationHeader());
  }

  GetImageUrl(username: string): string{
    return this.RootUrl + '/Users/Image/' + username;
  }

}
