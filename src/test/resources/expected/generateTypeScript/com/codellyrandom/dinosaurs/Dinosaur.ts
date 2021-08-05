import { Type } from "class-transformer"

import Period from "../geology/Period"
import { CommonTypes_Geolocation } from "./CommonTypes"

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
  @Type(() => CommonTypes_Geolocation)
  location?: CommonTypes_Geolocation = undefined
  diet_type?: Dinosaur_DietType = undefined
  @Type(() => Dinosaur_SleepSchedule)
  sleep_schedule?: Dinosaur_SleepSchedule = undefined
  @Type(() => Date)
  earliest_known_fossil?: Date = undefined

  // primary_defense_mechanism: At most one of these fields will be non-null
  @Type(() => Dinosaur_SharpTeeth)
  sharp_tail?: Dinosaur_SharpTeeth = undefined
  @Type(() => Dinosaur_SwingingTail)
  swinging_tail?: Dinosaur_SwingingTail = undefined
}

export class Dinosaur_SwingingTail {

}

export class Dinosaur_SharpTeeth {

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
