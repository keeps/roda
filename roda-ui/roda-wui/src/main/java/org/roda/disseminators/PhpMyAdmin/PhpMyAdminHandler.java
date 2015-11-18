/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.disseminators.PhpMyAdmin;

/**
 * @author Rui Castro
 * @author Luis Faria
 */
public class PhpMyAdminHandler { // extends Handler {
// private static final long serialVersionUID = 5323073072589613225L;
//
// private Logger logger = LoggerFactory.getLogger(PhpMyAdminHandler.class);
//
// private static final Pattern QUERY_DB_PATTERN = Pattern
// .compile("^db\\=([^(\\&)]*)");
//
// @Override
// protected void service(HttpServletRequest request,
// HttpServletResponse response) throws ServletException, IOException {
//
// String query = request.getQueryString();
//
// String db = null;
//
// if (query != null) {
// Matcher matcher = QUERY_DB_PATTERN.matcher(query);
// if (matcher.find()) {
// db = matcher.group(1);
// }
//
// // query string security check
// if (matcher.find()) {
// logger.warn("QUERY STRING INJECTION ATTEMPTED! IP:"
// + request.getRemoteAddr());
// response
// .sendError(HttpServletResponse.SC_UNAUTHORIZED,
// "Query string injection attempt detected, your IP address was recorded.");
// }
// }
//
// if (StringUtils.isBlank(db)) {
// super.service(request, response);
// } else {
// String pid = db.replace("_", ":");
// // try {
// //
// // RODAClient rodaClient = RodaClientFactory.getRodaClient(request
// // .getSession());
// // Browser browserService = rodaClient.getBrowserService();
// //
// // browserService.getSimpleRepresentationObject(pid);
// //
// // super.service(request, response);
// //
// // } catch (RODAClientException e) {
// // response.sendError(
// // HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
// // .getMessage());
// // response.flushBuffer();
// // } catch (BrowserException e) {
// // response.sendError(
// // HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
// // .getMessage());
// // response.flushBuffer();
// // } catch (LoginException e) {
// // response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e
// // .getMessage());
// // response.flushBuffer();
// // } catch (NoSuchRODAObjectException e) {
// // response.sendError(HttpServletResponse.SC_NOT_FOUND, e
// // .getMessage());
// // response.flushBuffer();
// // }
//
// }
//
// }
}
