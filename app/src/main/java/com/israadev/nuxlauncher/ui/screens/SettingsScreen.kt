                                }
                                Box(
                                    modifier = Modifier
                                        .background(NuxColors.ForestGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .border(1.dp, NuxColors.MintGreen.copy(alpha = 0.45f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (defaultRuntime == "auto") "AUTO" else "JAVA ${defaultRuntime.removePrefix("jre-")}",
                                        color = NuxColors.MintGreen,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(7.dp))

                            val javaRuntimeOptions = listOf(
                                "auto" to "AUTO",
                                "jre-8" to "JAVA 8",
                                "jre-17" to "JAVA 17",
                                "jre-21" to "JAVA 21",
                                "jre-25" to "JAVA 25",
                                "temurin-8" to "JAVA 8 TEMURIN",
                                "temurin-17" to "JAVA 17 TEMURIN"
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                javaRuntimeOptions.forEach { (runtimeId, label) ->
                                    val selected = defaultRuntime.equals(runtimeId, ignoreCase = true)