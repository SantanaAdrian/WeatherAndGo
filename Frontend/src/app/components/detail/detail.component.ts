import { Observable } from 'rxjs';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PokemonService } from 'src/app/services/weather.service';

@Component({
  selector: 'app-detail',
  templateUrl: './detail.component.html',
  styleUrls: ['./detail.component.css']
})
export class DetailComponent implements OnInit{

  id!:string;
  miPokemon:any={};

  constructor(private _route:ActivatedRoute, private _router:Router, private _PokemonService:PokemonService){}

  ngOnInit(): void {
    this.getDatos();
  }

  getDatos():void{

    //Cuando cambiamos de url el componente se recarga
    this._route.params.subscribe({
      next:(params) => {
        this.id = params['id'];
        //console.log(this.id);
      },
      error:(error) => {this._router.navigate(['error'])},
      complete:() => {console.log("El Observer ha recibido los parametros")}
    });

    this._PokemonService.getPokemon(this.id).subscribe({
      next:(result) => {
        console.log(result);
        this.miPokemon = result;
        //this.miPokemon.species.url ==> evolution chain;

           },
      error:(error) => {this._router.navigate(['error'])},
      complete:() => {console.log("El Observer ha recibido los parametros")}
    });

  }

}
