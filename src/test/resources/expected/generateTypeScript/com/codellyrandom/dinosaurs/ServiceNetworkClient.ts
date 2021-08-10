// A network response.
// AxiosResponse fulfills the requirements of this interface.  
export interface ServiceNetworkResponse<T = any> {
  data: T;
}

// A network client which can send requests.
// AxiosInstance fulfills the requirements of this interface and can be passed in via
//   (axios as ServiceNetwork)
export default interface ServiceNetworkClient {
  // Send a POST network request to a given path. 
  // The path will not include the domain and will be something like "/users/add"
  // The data will be the a JSON string to send as the request payload.
  post<T = any, R = ServiceNetworkResponse<T>>(path: string, data?: any): Promise<R>;
}