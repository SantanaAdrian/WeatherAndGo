import { GLOBAL } from './global';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PokemonService {

  url:string;
  constructor(private _http:HttpClient) {
    this.url=GLOBAL.API_URL;
  }
  getPokemons(nPokemon:number):Observable<any>{
    let headers = new HttpHeaders().set('content-type', 'application/json').set('Access-Control-Allow-Origin', '*');
    return this._http.get(this.url + "?limit=" + nPokemon + "&offset=0", {headers});
  }
  getPokemon(id:string):Observable<any>{
    let headers = new HttpHeaders().set('content-type', 'application/json').set('Access-Control-Allow-Origin', '*');
    return this._http.get(this.url + id, {headers});
  }
  //TODO crear metodos getEvolution y getEvolutionChain
  
}
