package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;

import java.util.Date;
import java.util.List;

public interface ShipService {

  //  void create(Ship ship);
    Ship createShip(Ship ship);

    Ship updateShip(Ship oldShip, Ship newShip) throws IllegalArgumentException;

    /**
     * Сохраняет корабль
     * @param ship
     * @return
     */
    Ship saveShip(Ship ship);

    Ship getShipById(Long id);


    /**
     * Возвращает список всех имеющихся кораблей
     * @return список клиентов
     */
    List<Ship> getAllShips(
            String name,
            String planet,
            ShipType shipType,
            Long after,
            Long before,
            Boolean isUsed,
            Double minSpeed,
            Double maxSpeed,
            Integer minCrewSize,
            Integer maxCrewSize,
            Double minRating,
            Double maxRating
    );


    /**
     * Удаление
     * @param ship
     */
    void deleteShip(Ship ship);

    /**
     * Сортировать корабли
     * @param ships
     * @param order
     * @return
     */
    List<Ship> sortShips(List<Ship> ships, ShipOrder order);

    List<Ship> getPage(List<Ship> ships, Integer pageNumber, Integer pageSize);

    boolean isShipValid(Ship ship);

    /**
     * вычисляем рэйтинг корабля     *
     * @return
     */
    double computeRating(Ship ship);//double speed, boolean isUsed, Date prod);


}
