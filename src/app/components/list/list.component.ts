import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { GLOBAL } from 'src/app/services/global';
import { PokemonService } from 'src/app/services/pokemon.service';

@Component({
  selector: 'app-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.css']
})
export class ListComponent implements OnInit{

  listaPokemons: Array<any> = [];

constructor(private _pokemonService:PokemonService, private _router:Router){
}

  ngOnInit(): void {
    this.getDatos();
  }

  getDatos():void{
    this._pokemonService.getPokemons(36).subscribe({
      next: (result) => {
        console.log(result.results)
        this.listaPokemons = result.results;
        for(let pokemon of this.listaPokemons){
          pokemon.id = this.extraerIdPokemon(pokemon.url);
          pokemon.urlImagen = GLOBAL.IMAGEN_URL + pokemon.id + ".png";
        }
      },
      error: (error) => {
        this._router.navigate(['error']);
      },
      complete: () => {
        console.log("El Observer ha recibido los datos");
      }
    });
  }

  extraerIdPokemon(url:string):string{

    let id : string = url.slice(34,-1);
    return id;
  }

}
