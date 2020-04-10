package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ShipServiceImpl implements ShipService {

    private ShipRepository shipRepository;

    public ShipServiceImpl() {

    }

    @Autowired
    public ShipServiceImpl(ShipRepository shipRepository) {
       // super();
        this.shipRepository = shipRepository;
    }

    @Override
    public Ship createShip(Ship ship) {

        if (ship.getName() == null || ship.getPlanet() == null
                || ship.getShipType() == null || ship.getProdDate() == null
                || ship.getSpeed() == null || ship.getCrewSize() == null)
            throw new IllegalArgumentException();//throw new BadRequestException("One of Ship params is null");

        //checkShipParams(ship);
        if(!isShipValid(ship)) throw new IllegalArgumentException();

        if (ship.getUsed() == null)
            ship.setUsed(false);

        Double raiting = computeRating(ship);
        ship.setRating(raiting);

        return shipRepository.saveAndFlush(ship);
    }


    /**
     * Обновлять нужно только те поля, которые не null.
     * Если корабль не найден в БД, необходимо ответить ошибкой с кодом 404.
     * Если значение id не валидное, необходимо ответить ошибкой с кодом 400.
     * @param oldShip
     * @param newShip
     * @return
     * @throws IllegalArgumentException
     */
    @Override
    public Ship updateShip(Ship oldShip, Ship newShip) throws IllegalArgumentException {

        boolean changeRating = false;

        if (newShip.getName()!=null ) {
            if (isNameLengthValid(newShip.getName())) {
                oldShip.setName(newShip.getName());
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (newShip.getPlanet()!=null) {
            if (isPlanetLengthValid(newShip.getPlanet())) {
                oldShip.setPlanet(newShip.getPlanet());
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (newShip.getShipType() != null) {
            oldShip.setShipType(newShip.getShipType());
        }

        if (newShip.getProdDate()!=null) {
            if (isDateValid(newShip.getProdDate())) {
                oldShip.setProdDate(newShip.getProdDate());
                changeRating = true;
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (newShip.getUsed() != null) {
            oldShip.setUsed(newShip.getUsed());
            changeRating = true;
        }
        if (newShip.getSpeed()!=null) {
            if (isSpeedValid(newShip.getSpeed())) {
                    oldShip.setSpeed(newShip.getSpeed());
                    changeRating = true;
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (newShip.getCrewSize()!=null) {
            if (isCrewSizeValid(newShip.getCrewSize())) {
                oldShip.setCrewSize(newShip.getCrewSize());
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (changeRating) {
            oldShip.setRating(computeRating(oldShip));//oldShip.getSpeed(), oldShip.getUsed(), oldShip.getProdDate()));
        }
        shipRepository.save(oldShip);
        return oldShip;

    }

    @Override
    public Ship saveShip(Ship ship) {
        //тут мы сохраняем Ship, используя просто save() мы сохраняем запись;
        Ship savedShip = shipRepository.save(ship);
        return savedShip;
    }

    @Override
    public Ship getShipById(Long id) {
        //
        return shipRepository.findById(id).orElse(null);
    }

    /**
     * Поиск по полям name и planet происходить по частичному соответствию.
     * Например, если в БД есть корабль с именем «Левиафан», а параметр name задан как «иа» -
     * такой корабль должен отображаться в результатах (Левиафан).
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
     * @return
     */

    @Override
    public List<Ship> getAllShips(String name,
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
                               Double maxRating)
    {

        List<Ship> ships = shipRepository.findAll();

        if (name != null)
        {
            ships = ships.stream().filter(ship -> ship.getName().toLowerCase()
                    .contains(name.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (planet != null)
        {
            ships = ships.stream().filter(ship -> ship.getPlanet().toLowerCase()
                    .contains(planet.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (shipType != null)
        {
            ships = ships.stream().filter(ship -> ship.getShipType().equals(shipType))
                    .collect(Collectors.toList());
        }

        if (after != null)
        {
            ships = ships.stream().filter(ship -> ship.getProdDate().after(new Date(after)))
                    .collect(Collectors.toList());
        }

        if (before != null)
        {
            ships = ships.stream().filter(ship -> ship.getProdDate().before(new Date(before)))
                    .collect(Collectors.toList());
        }

        if (isUsed != null)
        {
            ships = ships.stream().filter(ship -> ship.getUsed().equals(isUsed))
                    .collect(Collectors.toList());
        }

        if (minSpeed != null)
        {
            ships = ships.stream().filter(ship -> ship.getSpeed() >= minSpeed)
                    .collect(Collectors.toList());
        }

        if (maxSpeed != null)
        {
            ships = ships.stream().filter(ship -> ship.getSpeed() <= maxSpeed)
                    .collect(Collectors.toList());
        }

        if (minCrewSize != null)
        {
            ships = ships.stream().filter(ship -> ship.getCrewSize() >= minCrewSize)
                    .collect(Collectors.toList());
        }

        if (maxCrewSize != null)
        {
            ships = ships.stream().filter(ship -> ship.getCrewSize() <= maxCrewSize)
                    .collect(Collectors.toList());
        }

        if (minRating != null)
        {
            ships = ships.stream().filter(ship -> ship.getRating() >= minRating)
                    .collect(Collectors.toList());
        }

        if (maxRating != null)
        {
            ships = ships.stream().filter(ship -> ship.getRating() <= maxRating)
                    .collect(Collectors.toList());
        }

        return ships;

    }


    @Override
    public void deleteShip(Ship ship) {
        shipRepository.delete(ship);

    }
//сортировка по enum ShipOrder
    @Override
    public List<Ship> sortShips(List<Ship> ships, ShipOrder order) {
        if (order != null) {
            ships.sort((ship1, ship2) -> {
                switch (order) {
                    case ID: return ship1.getId().compareTo(ship2.getId());
                    case SPEED: return ship1.getSpeed().compareTo(ship2.getSpeed());
                    case DATE: return ship1.getProdDate().compareTo(ship2.getProdDate());
                    case RATING: return ship1.getRating().compareTo(ship2.getRating());
                    default: return 0;
                }
            });
        }
        return ships;
    }

    @Override
    public List<Ship> getPage(List<Ship> ships, Integer pageNumber, Integer pageSize) {
        Integer page = pageNumber == null ? 0 : pageNumber;
        Integer size = pageSize == null ? 3 : pageSize;
        int first = page * size;
        int last = first + size;
        if (last > ships.size())
        {
            last = ships.size();
        }
        return ships.subList(first, last);
    }

    @Override
    public boolean isShipValid(Ship ship) {
        return ship != null && isNameLengthValid(ship.getName()) && isPlanetLengthValid(ship.getPlanet())
                && isSpeedValid(ship.getSpeed()) && isDateValid(ship.getProdDate())
                && isCrewSizeValid(ship.getCrewSize());
    }

    public boolean isNameLengthValid(String name) {
        return  name.length() >= 1 && name.length() <= 50; //Название корабля (до 50 знаков включительно) name != null &&
    }

    public boolean isPlanetLengthValid(String planet) {
        return  planet.length() >= 1 && planet.length() <= 50; //Планета пребывания (до 50 знаков включительно) planet != null &&
    }

    public boolean isSpeedValid(Double speed){
        return  round(speed) >= 0.01 && round(speed) <= 0.99; //Диапазон значений 0,01..0,99 включительно. speed != null &&
    }

    private boolean isDateValid(Date prodDate) {
        int iProdDate = getYearFromDate(prodDate);
        return  iProdDate >= 2800 && iProdDate <= 3019; //Диапазон значений года 2800..3019 включительно prodDate != null &&
    }

    public static int getYearFromDate(Date date) {
        int result = -1;
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            result = cal.get(Calendar.YEAR);
        }
        return result;
    }

    private boolean isCrewSizeValid(Integer crewSize) {
        return  crewSize >= 1 && crewSize <= 9999; //Количество членов экипажа, Диапазон значений 1..9999 включительно. crewSize != null &&
    }

    /**
     * Рейтинг корабля рассчитывается по формуле:
     * 𝑅=80·𝑣·𝑘/𝑦0−𝑦1+1 ,
     * где:
     * v — скорость корабля;
     * k — коэффициент, который равен 1 для нового корабля и 0,5 для использованного;
     * y0 — текущий год (не забудь, что «сейчас» 3019 год);
     * y1 — год выпуска корабля.
     */
    @Override
    public double computeRating(Ship ship){
       // Calendar cal = Calendar.getInstance();
       // cal.setTime(ship.getProdDate());
        int year = getYearFromDate(ship.getProdDate());//cal.get(Calendar.YEAR);
        double k = ship.getUsed() ? 0.5:1;
        //BigDecimal raiting = new BigDecimal((80 * ship.getSpeed() * k / (3019 - year + 1)));
        double raiting = (80 * ship.getSpeed() * k / (3019 - year + 1));
        //raiting = raiting.setScale(2, RoundingMode.HALF_UP);
        return round(raiting);//raiting.doubleValue();
    }

    /**
     * математическое округление до сотых
     */
    private double round(double value) {
        return Math.round(value * 100) / 100D;
    }
}
