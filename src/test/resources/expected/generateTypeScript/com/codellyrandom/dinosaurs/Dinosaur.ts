import { Type } from "class-transformer"

import Period from "../geology/Period"
import Geolocation from "./Geolocation"

// A now-extinct lizard-like creature.
// It's likely that they went extinct due to the impact of
// a large meteor on earth.
export default class Dinosaur {
  // Common name of this dinosaur
  // For example, "Stegosaurus".
  name?: string = undefined
  // URLs with images of this dinosaur.
  picture_urls?: string[] = undefined
  length_meters?: number = undefined
  mass_kilograms?: number = undefined
  period?: Period = undefined
  @Type(() => Geolocation)
  location?: Geolocation = undefined
  diet_type?: Dinosaur_DietType = undefined
  @Type(() => Dinosaur_SleepSchedule)
  sleep_schedule?: Dinosaur_SleepSchedule = undefined
}

export enum Dinosaur_DietType {
  UNKNOWN = "UNKNOWN",
  VEGETARIAN = "VEGETARIAN",
  CARNIVORE = "CARNIVORE",
  OMNIVORE = "OMNIVORE",
}

export class Dinosaur_SleepSchedule {
  @Type(() => Dinosaur_SleepSchedule_TimeSpan)
  awake?: Dinosaur_SleepSchedule_TimeSpan[] = undefined
}

export class Dinosaur_SleepSchedule_TimeSpan {
  start: number
  end: number
}
