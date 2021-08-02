import Period from "../geology/Period"

// A now-extinct lizard-like creature.
export default class Dinosaur {
  // Common name of this dinosaur, like "Stegosaurus".
  name?: string = undefined
  // URLs with images of this dinosaur.
  picture_urls?: string[] = undefined
  length_meters?: number = undefined
  mass_kilograms?: number = undefined
  period?: Period = undefined
  location?: Geolocation = undefined
  diet_type?: Dinosaur_DietType = undefined
  sleep_schedule?: Dinosaur_SleepSchedule = undefined
}

export enum Dinosaur_DietType {
  UNKNOWN = "UNKNOWN",
  VEGETARIAN = "VEGETARIAN",
  CARNIVORE = "CARNIVORE",
  OMNIVORE = "OMNIVORE",
}

export class Dinosaur_SleepSchedule {
  awake?: Dinosaur_SleepSchedule_TimeSpan[] = undefined
}

export class Dinosaur_SleepSchedule_TimeSpan {
  start: number
  end: number
}
