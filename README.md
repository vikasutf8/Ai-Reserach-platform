# Ai-Reserach-platform
Scraping required company via rotating proxies for IP and pass to antropic for analysis and serve to Angular dashboard via PG presistence datastore 


<img width="1993" height="1110" alt="image" src="https://github.com/user-attachments/assets/0fd4df7d-49a7-4deb-acc8-22f911bd64f0" />


# Models define ;-
### ScrapedPost
1. Plafform  [REDITT,PRODUCTHUT,HACKERRANK]
2. externalID 
3. title
4. content -columndefinition  =text
5. url
6. author
7. score
8. commentCount
9. subReddit
10. proxyIpUsed 
11. postedAt
12. scrapedAt


### TrendAnalysis
1. rowAnalysis 
2. Platform 
3. postAnalysis -->how many post
4. analysisAt

### TrendTopic
1. topic -->what is trending topics
2. summary
3. reasoning --->LLM
4. category ---> AI/SAAS
5. mentionCount
6. score
7. Platform primaryPlatform
8. samplePostIds
9. TrendAnalysis analysis --> many to one 
10. detectedAt
