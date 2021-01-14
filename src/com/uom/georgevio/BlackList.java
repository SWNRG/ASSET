package com.uom.georgevio;

/* Code omitted since this part is going commercial
 * You can always create one, based on the pseudocode 
 * and the logic described in paper 
 * "ASSET: A Softwarized Intrusion Detection System for RPL":
 * 
 * Algorithm 1: Parent selection considering blacklisted nodes
Result: Selects the best parent that is safe
Input : Candidate parents p1 and p2
Output: Safe parent
1 begin
2 if p1 in blacklist[] then
3 if p2 in blacklist[] then
4 return null;
5 else
6 return p2;
7 end
8 else
9 if p2 in blacklist[] then
10 return p1;
11 else
12 // Choose parent process of standard RPL-MRHOF
objective function
13 return p1.ETX < p2.ETX ? p1 : p2;
14 end
15 end
16 end
 */
public class BlackList {

}
