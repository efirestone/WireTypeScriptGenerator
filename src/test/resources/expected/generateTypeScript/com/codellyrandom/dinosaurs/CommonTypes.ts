export class CommonTypes {}

export default CommonTypes

export class CommonTypes_Geolocation {
  latitude: number
  longitude: number

  constructor(latitude: number, longitude: number) {
    this.latitude = latitude
    this.longitude = longitude
  }
}
