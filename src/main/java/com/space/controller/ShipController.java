package com.space.controller;


import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ShipController {

    private ShipService shipService;

    public ShipController() {
    }

    @Autowired
    public ShipController(ShipService shipService) {
        this.shipService = shipService;
    }

    /**
     * Создание корабля.
     * Мы не можем создать корабль, если:
     * - указаны не все параметры из Data Params (кроме isUsed);
     * - длина значения параметра “name” или “planet” превышает размер соответствующего поля в БД (50 символов);
     * - значение параметра “name” или “planet” пустая строка;
     * - скорость или размер команды находятся вне заданных пределов;
     * - “prodDate”:[Long] < 0;
     * - год производства находятся вне заданных пределов.
     * В случае всего вышеперечисленного необходимо ответить ошибкой с кодом 400.
     * @param ship
     * @return
     */
    @RequestMapping(path = "/rest/ships", method = RequestMethod.POST) //"/rest/ships"
    @ResponseBody
    public ResponseEntity<Ship> createShip(@RequestBody Ship ship) {
        Ship savedShip;
        try {
            savedShip = shipService.createShip(ship);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(savedShip, HttpStatus.OK);
    }

    /**
     * Поиск по полям name и planet происходить по частичному соответствию. Например, если в БД есть корабль с именем
     * «Левиафан», а параметр name задан как «иа» - такой корабль должен отображаться в результатах (Левиафан).
     * pageNumber – параметр, который отвечает за номер отображаемой страницы при использовании пейджинга.
     * Нумерация начинается с нуля
     * pageSize – параметр, который отвечает за количество результатов на одной странице при пейджинге
     * @param name
     * @param planet
     * @param shipType
     * @param after
     * @param before
     * @param isUsed
     * @param minSpeed
     * @param maxSpeed
     * @param minCrewSize
     * @param maxCrewSize
     * @param minRating
     * @param maxRating
     * @param order
     * @param pageNumber
     * @param pageSize
     * @return
     */
    @RequestMapping(path = "/rest/ships", method = RequestMethod.GET)
    public List<Ship> getAllShips(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "planet", required = false) String planet,
            @RequestParam(value = "shipType", required = false) ShipType shipType,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "isUsed", required = false) Boolean isUsed,
            @RequestParam(value = "minSpeed", required = false) Double minSpeed,
            @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
            @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
            @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
            @RequestParam(value = "minRating", required = false) Double minRating,
            @RequestParam(value = "maxRating", required = false) Double maxRating,
            @RequestParam(value = "order", required = false) ShipOrder order,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize
    ) {
        List<Ship> ships = shipService.getAllShips(
                name, planet, shipType, after,
                before, isUsed, minSpeed, maxSpeed,
                minCrewSize, maxCrewSize, minRating, maxRating
        );
        //Получаем отсортированный список по order
        List<Ship> sortedShips = shipService.sortShips(ships, order);
        // указываем номер страницы и максимальное кол-во на ней
        return shipService.getPage(sortedShips, pageNumber, pageSize);
    }

  @RequestMapping(path = "/rest/ships/count", method = RequestMethod.GET)
    public Integer getShipsCount(
          @RequestParam(name = "name", required = false) String name,
          @RequestParam(name = "planet", required = false) String planet,
          @RequestParam(name = "shipType", required = false) ShipType shipType,
          @RequestParam(name = "after", required = false) Long after,
          @RequestParam(name = "before", required = false) Long before,
          @RequestParam(name = "isUsed", required = false) Boolean isUsed,
          @RequestParam(name = "minSpeed", required = false) Double minSpeed,
          @RequestParam(name = "maxSpeed", required = false) Double maxSpeed,
          @RequestParam(name = "minCrewSize", required = false) Integer minCrewSize,
          @RequestParam(name = "maxCrewSize", required = false) Integer maxCrewSize,
          @RequestParam(name = "minRating", required = false) Double minRating,
          @RequestParam(name = "maxRating", required = false) Double maxRating
  ){

      return shipService.getAllShips(name, planet, shipType, after, before, isUsed, minSpeed, maxSpeed,
              minCrewSize, maxCrewSize, minRating, maxRating).size();

  }

    @RequestMapping(path = "/rest/ships/{id}", method = RequestMethod.GET)
    public ResponseEntity<Ship> getShip(@PathVariable(value = "id") Long id) {
        //Если значение id не валидное, необходимо ответить ошибкой с кодом 400.
        if (id == null || id <= 0)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Ship ship = shipService.getShipById(id);
        return  ship == null  //Если корабль не найден в БД, необходимо ответить ошибкой с кодом 404.
            ? new ResponseEntity<>(HttpStatus.NOT_FOUND)
            : new ResponseEntity<>(ship, HttpStatus.OK);
    }
//Обновлять нужно только те поля, которые не null. -> go to ShipServiceImpl


    @RequestMapping(path = "/rest/ships/{id}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Ship> updateShip(@PathVariable(value = "id") Long id,
            @RequestBody Ship ship
    ) {
        ResponseEntity<Ship> entity = getShip(id);

        if(entity == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND); //Если корабль не найден в БД, необходимо ответить ошибкой с кодом 404.

        Ship savedShip = entity.getBody();
        if (savedShip == null) {
            return entity;
        }

        Ship result;
        try {
            result = shipService.updateShip(savedShip, ship);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);//Если значение id не валидное, необходимо ответить ошибкой с кодом 400.
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
//Если корабль не найден в БД, необходимо ответить ошибкой с кодом 404.
//Если значение id не валидное, необходимо ответить ошибкой с кодом 400.
    @RequestMapping(path = "/rest/ships/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Ship> deleteShip(@PathVariable(value = "id") Long id) {
        ResponseEntity<Ship> entity = getShip(id);
        if(entity == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND); //Если корабль не найден в БД, необходимо ответить ошибкой с кодом 404.
        Ship savedShip = entity.getBody();
        if (savedShip == null) {
            return entity;
        }
        shipService.deleteShip(savedShip);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
