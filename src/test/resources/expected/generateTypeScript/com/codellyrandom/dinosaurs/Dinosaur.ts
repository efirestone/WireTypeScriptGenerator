import { Type } from "class-transformer"

import Period from "../geology/Period"
import { CommonTypes_Geolocation } from "./CommonTypes"

// A now-extinct lizard-like creature.
// It's likely that they went extinct due to the impact of
// a large meteor on earth.
export class Dinosaur {
  // Common name of this dinosaur
  // For example, "Stegosaurus".
  name?: string = undefined
  // URLs with images of this dinosaur.
  picture_urls: string[] = []
  length_meters?: number = undefined
  mass_kilograms?: number = undefined
  period?: Period = undefined
  @Type(() => CommonTypes_Geolocation)
  location?: CommonTypes_Geolocation = undefined
  diet_type?: Dinosaur_DietType = undefined
  @Type(() => Dinosaur_DefenseMechanism)
  defense_mechanism?: Dinosaur_DefenseMechanism = undefined
  @Type(() => Dinosaur_SleepSchedule)
  sleep_schedule?: Dinosaur_SleepSchedule = undefined
  @Type(() => Date)
  earliest_known_fossil?: Date = undefined

  constructor(configure: ((o: Dinosaur) => void) | undefined = undefined) {
    configure?.call(this, this)
  }
}

export default Dinosaur

export class Dinosaur_DefenseMechanism {
  // defense: At most one of these fields will be non-null
  @Type(() => Dinosaur_DefenseMechanism_SharpTeeth)
  sharp_tail?: Dinosaur_DefenseMechanism_SharpTeeth = undefined
  @Type(() => Dinosaur_DefenseMechanism_SwingingTail)
  swinging_tail?: Dinosaur_DefenseMechanism_SwingingTail = undefined

  constructor(configure: ((o: Dinosaur_DefenseMechanism) => void) | undefined = undefined) {
    configure?.call(this, this)
  }
}

export class Dinosaur_DefenseMechanism_SwingingTail {}

export class Dinosaur_DefenseMechanism_SharpTeeth {}

export enum Dinosaur_DietType {
  UNKNOWN = "UNKNOWN",
  VEGETARIAN = "VEGETARIAN",
  CARNIVORE = "CARNIVORE",
  OMNIVORE = "OMNIVORE",
}

export class Dinosaur_SleepSchedule {
  name: string
  @Type(() => Dinosaur_SleepSchedule_TimeSpan)
  awake: Dinosaur_SleepSchedule_TimeSpan[] = []

  constructor(name: string, configure: ((o: Dinosaur_SleepSchedule) => void) | undefined = undefined) {
    this.name = name
    configure?.call(this, this)
  }
}

export class Dinosaur_SleepSchedule_TimeSpan {
  start: number
  end: number

  constructor(start: number, end: number) {
    this.start = start
    this.end = end
  }
}
